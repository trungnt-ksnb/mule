/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.expression;

public class ExceptionTypeExpressionEvaluator extends IllegalExpressionEvaluator
{

    public static final String NAME = "exception-type";

    @Override
    public String getName()
    {
        return NAME;
    }
}