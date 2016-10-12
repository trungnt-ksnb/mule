/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.transport;


import org.mule.runtime.core.util.IOUtils;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.OutputStream;

public class WsHttpDispatcher {

  public WsHttpDispatcher() {

  }

  public String dispatch(OutputStream os) {
    OkHttpClient client = new OkHttpClient();
    MediaType mediaType = MediaType.parse("text/xml");
    RequestBody body = RequestBody.create(mediaType, os.toString());
    Request request = new Request.Builder()
        .url("http://localhost:6045/testService")
        .post(body)
        .addHeader("content-type", "text/xml")
        .addHeader("cache-control", "no-cache")
        .build();

    try {
      Response response = client.newCall(request).execute();
      return IOUtils.toString(response.body().byteStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
