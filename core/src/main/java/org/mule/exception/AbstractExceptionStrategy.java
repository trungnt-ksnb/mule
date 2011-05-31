/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.exception;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.context.notification.ServerNotification;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.security.SecurityException;
import org.mule.api.transaction.RollbackMethod;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.util.StreamCloserService;
import org.mule.config.ExceptionHelper;
import org.mule.context.notification.ExceptionNotification;
import org.mule.context.notification.SecurityNotification;
import org.mule.management.stats.FlowConstructStatistics;
import org.mule.management.stats.ServiceStatistics;
import org.mule.message.ExceptionMessage;
import org.mule.processor.AbstractMessageProcessorOwner;
import org.mule.routing.filters.WildcardFilter;
import org.mule.routing.outbound.MulticastingRouter;
import org.mule.transaction.TransactionCoordination;
import org.mule.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the base class for exception strategies which contains several helper methods.  However, you should 
 * probably inherit from <code>AbstractMessagingExceptionStrategy</code> (if you are creating a Messaging Exception Strategy) 
 * or <code>AbstractSystemExceptionStrategy</code> (if you are creating a System Exception Strategy) rather than directly from this class.
 */
public abstract class AbstractExceptionStrategy extends AbstractMessageProcessorOwner
{
    protected transient Log logger = LogFactory.getLog(getClass());

    @SuppressWarnings("unchecked")
    protected List<MessageProcessor> messageProcessors = new CopyOnWriteArrayList();

    protected AtomicBoolean initialised = new AtomicBoolean(false);

    protected WildcardFilter rollbackTxFilter;
    protected WildcardFilter commitTxFilter;

    protected boolean enableNotifications = true;

    public AbstractExceptionStrategy(MuleContext muleContext, boolean rollbackByDefault)
    {
        super();
        setMuleContext(muleContext);
        if (rollbackByDefault)
        {
            setRollbackTxFilter(new WildcardFilter("*"));
        }
    }
    
    protected boolean isRollback(Throwable t)
    {
        return (rollbackTxFilter != null && rollbackTxFilter.accept(t.getClass().getName())) ||
            (commitTxFilter != null && !commitTxFilter.accept(t.getClass().getName()));
    }
    
    public List<MessageProcessor> getMessageProcessors()
    {
        return messageProcessors;
    }

    public void setMessageProcessors(List<MessageProcessor> processors)
    {
        if (processors != null)
        {
            this.messageProcessors.clear();
            this.messageProcessors.addAll(processors);
        }
        else
        {
            throw new IllegalArgumentException("List of targets = null");
        }
    }

    public void addEndpoint(MessageProcessor processor)
    {
        if (processor != null)
        {
            messageProcessors.add(processor);
        }
    }

    public boolean removeMessageProcessor(MessageProcessor processor)
    {
        return messageProcessors.remove(processor);
    }

    protected Throwable getExceptionType(Throwable t, Class exceptionType)
    {
        while (t != null)
        {
            if (exceptionType.isAssignableFrom(t.getClass()))
            {
                return t;
            }

            t = t.getCause();
        }

        return null;
    }

    /**
     * The initialise method is call every time the Exception stategy is assigned to
     * a service or connector. This implementation ensures that initialise is called
     * only once. The actual initialisation code is contained in the
     * <code>doInitialise()</code> method.
     * 
     * @throws InitialisationException
     */
    public final synchronized void initialise() throws InitialisationException
    {
        super.initialise();
        if (!initialised.get())
        {
            doInitialise(muleContext);
            initialised.set(true);
        }
    }

    protected void doInitialise(MuleContext muleContext) throws InitialisationException
    {
        logger.info("Initialising exception listener: " + toString());
    }

    protected void fireNotification(Exception ex)
    {
        if (enableNotifications)
        {
            if (ex instanceof SecurityException)
            {
                fireNotification(new SecurityNotification((SecurityException) ex, SecurityNotification.SECURITY_AUTHENTICATION_FAILED));
            }
            else
            {
                fireNotification(new ExceptionNotification(ex));
            }
        }
    }

    /**
     * Routes the current exception to an error endpoint such as a Dead Letter Queue
     * (jms) This method is only invoked if there is a MuleMessage available to
     * dispatch. The message dispatched from this method will be an
     * <code>ExceptionMessage</code> which contains the exception thrown the
     * MuleMessage and any context information.
     * 
     * @param message the MuleMessage being processed when the exception occurred
     * @param target optional; the endpoint being dispatched or received on
     *            when the error occurred. This is NOT the endpoint that the message
     *            will be disptched on and is only supplied to this method for
     *            logging purposes
     * @param t the exception thrown. This will be sent with the ExceptionMessage
     * @see ExceptionMessage
     */
    protected void routeException(MuleEvent event, Throwable t)
    {
        if (!messageProcessors.isEmpty())
        {
            try
            {
                logger.error("Message being processed is: " + (event.getMessage().getPayloadForLogging()));
                String component = "Unknown";
                if (event.getFlowConstruct() != null)
                {
                    component = event.getFlowConstruct().getName();
                }
                EndpointURI endpointUri = event.getEndpoint().getEndpointURI();

                // Create an ExceptionMessage which contains the original payload, the exception, and some additional context info.
                ExceptionMessage msg = new ExceptionMessage(event, t, component, endpointUri);
                MuleMessage exceptionMessage = new DefaultMuleMessage(msg, event.getMessage(), muleContext);

                // Create an outbound router with all endpoints configured on the exception strategy
                MulticastingRouter router = new MulticastingRouter()
                {
                    @Override
                    protected void setMessageProperties(FlowConstruct session, MuleMessage message, MessageProcessor target)
                    {
                        // No reply-to or correlation for exception targets, at least for now anyway.
                    }
                };
                router.setRoutes(getMessageProcessors());
                router.setMuleContext(muleContext);
                
                // Route the ExceptionMessage to the new router
                router.process(new DefaultMuleEvent(exceptionMessage, event));
            }
            catch (Exception e)
            {
                logFatal(event, e);
            }
        }
        
        List<MessageProcessor> processors = getMessageProcessors();
        FlowConstructStatistics statistics = event.getFlowConstruct().getStatistics();
        if (CollectionUtils.isNotEmpty(processors) && statistics instanceof ServiceStatistics)
        {
            if (statistics.isEnabled())
            {
                for (MessageProcessor endpoint : processors)
                {
                    ((ServiceStatistics) statistics).getOutboundRouterStat().incrementRoutedMessage(endpoint);
                }
            }
        }
    }

    protected void commit()
    {
        Transaction tx = TransactionCoordination.getInstance().getTransaction();
        if (tx != null)
        {
            try
            {
                tx.commit();
            }
            catch (TransactionException e)
            {
                logger.error(e);
            }
        }
    }

    protected void rollback(RollbackMethod rollbackMethod)
    {
        Transaction tx = TransactionCoordination.getInstance().getTransaction();
        if (tx != null)
        {
            try
            {
                tx.rollback();
                
                // TODO The following was in the catch clause of TransactionTemplate previously.  
                // Do we need to do this here?  If so, where can we store these variables (suspendedXATx, joinedExternal)
                // so that they are available to us in the exception handler?
                //
                //if (suspendedXATx != null)
                //{
                //  resumeXATransaction(suspendedXATx);
                //}
                //if (joinedExternal != null)
                //{
                //    TransactionCoordination.getInstance().unbindTransaction(joinedExternal);
                //}
            }
            catch (TransactionException e)
            {
                logger.error(e);
            }
        }
        else if (rollbackMethod != null)
        {
            rollbackMethod.rollback();
        }
    }

    protected void closeStream(MuleMessage message)
    {
        if (muleContext == null || muleContext.isDisposing() || muleContext.isDisposed())
        {
            return;
        }
        if (message != null
            && muleContext.getRegistry().lookupObject(MuleProperties.OBJECT_MULE_STREAM_CLOSER_SERVICE) != null)
        {
            ((StreamCloserService) muleContext.getRegistry().lookupObject(
                    MuleProperties.OBJECT_MULE_STREAM_CLOSER_SERVICE)).closeStream(message.getPayload());
        }
    }

    /**
     * Used to log the error passed into this Exception Listener
     * 
     * @param t the exception thrown
     */
    protected void logException(Throwable t)
    {
        MuleException muleException = ExceptionHelper.getRootMuleException(t);
        if (muleException != null)
        {
            logger.error(muleException.getDetailedMessage());
        }
        else
        {
            logger.error("Caught exception in Exception Strategy: " + t.getMessage(), t);
        }
    }

    /**
     * Logs a fatal error message to the logging system. This should be used mostly
     * if an error occurs in the exception listener itself. This implementation logs
     * the the message itself to the logs if it is not null
     * 
     * @param message The MuleMessage currently being processed
     * @param t the fatal exception to log
     */
    protected void logFatal(MuleEvent event, Throwable t)
    {
        FlowConstructStatistics statistics = event.getFlowConstruct().getStatistics();
        if (statistics != null && statistics.isEnabled())
        {
            statistics.incFatalError();
        }

        logger.fatal(
            "Failed to dispatch message to error queue after it failed to process.  This may cause message loss."
                            + (event.getMessage() == null ? "" : "Logging Message here: \n" + event.getMessage().toString()), t);
    }

    public boolean isInitialised()
    {
        return initialised.get();
    }

    /**
     * Fires a server notification to all registered
     * {@link org.mule.api.context.notification.ExceptionNotificationListener}
     * eventManager.
     * 
     * @param notification the notification to fire.
     */
    protected void fireNotification(ServerNotification notification)
    {
        if (muleContext != null)
        {
            muleContext.fireNotification(notification);
        }
        else if (logger.isWarnEnabled())
        {
            logger.debug("MuleContext is not yet available for firing notifications, ignoring event: "
                         + notification);
        }
    }

    public WildcardFilter getCommitTxFilter()
    {
        return commitTxFilter;
    }

    public void setCommitTxFilter(WildcardFilter commitTxFilter)
    {
        this.commitTxFilter = commitTxFilter;
    }

    public boolean isEnableNotifications()
    {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications)
    {
        this.enableNotifications = enableNotifications;
    }

    public WildcardFilter getRollbackTxFilter()
    {
        return rollbackTxFilter;
    }

    public void setRollbackTxFilter(WildcardFilter rollbackTxFilter)
    {
        this.rollbackTxFilter = rollbackTxFilter;
    }
    
    @Override
    protected List<MessageProcessor> getOwnedMessageProcessors()
    {
        return messageProcessors;
    }
}
