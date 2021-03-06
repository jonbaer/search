/*
 * Copyright 2013 Cloudera Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flume.sink.solr.morphline;

import java.util.UUID;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import com.fasterxml.uuid.UUIDType;

public class TestUUIDInterceptor extends Assert {

  private static final String ID = "id";

  @Test
  public void testBasic() throws Exception {
    Context context = new Context();
    context.put(UUIDInterceptor.HEADER_NAME, ID);
    context.put(UUIDInterceptor.PRESERVE_EXISTING_NAME, "true");
    Event event = new SimpleEvent();
    assertTrue(build(context).intercept(event).getHeaders().get(ID).length() > 0);
    assertTrue(buildParanoid(context).intercept(event).getHeaders().get(ID).length() > 0);
  }

  @Test
  public void testPreserveExisting() throws Exception {
    Context context = new Context();
    context.put(UUIDInterceptor.HEADER_NAME, ID);
    context.put(UUIDInterceptor.PRESERVE_EXISTING_NAME, "true");
    Event event = new SimpleEvent();
    event.getHeaders().put(ID, "foo");
    assertEquals("foo", build(context).intercept(event).getHeaders().get(ID));
    assertEquals("foo", buildParanoid(context).intercept(event).getHeaders().get(ID));
  }

  @Test
  public void testPrefix() throws Exception {
    Context context = new Context();
    context.put(UUIDInterceptor.HEADER_NAME, ID);
    context.put(UUIDInterceptor.PREFIX_NAME, "bar#");
    Event event = new SimpleEvent();
    assertTrue(build(context).intercept(event).getHeaders().get(ID).startsWith("bar#"));
    assertTrue(buildParanoid(context).intercept(event).getHeaders().get(ID).startsWith("bar#"));
  }

  private UUIDInterceptor build(Context context) {
    UUIDInterceptor.Builder builder = new UUIDInterceptor.Builder();
    builder.configure(context);
    return builder.build();
  }

  private ParanoidUUIDInterceptor buildParanoid(Context context) {
    ParanoidUUIDInterceptor.Builder builder = new ParanoidUUIDInterceptor.Builder();
    builder.configure(context);
    return builder.build();
  }

  // @Test
  public void testUUIDGenerationPerformance() throws Exception {
    testUUIDGenerationPerformance(Generators.timeBasedGenerator(EthernetAddress.fromInterface()));
    testUUIDGenerationPerformance(Generators.randomBasedGenerator());
    testUUIDGenerationPerformance(new NoArgGenerator() {
      @Override
      public UUIDType getType() {
        return UUIDType.RANDOM_BASED;
      }

      @Override
      public UUID generate() {
        return UUID.randomUUID();
      }
    });
  }

  private void testUUIDGenerationPerformance(NoArgGenerator uuidGenerator) throws Exception {
    long xstartTime = System.currentTimeMillis();
    int iters = 1000000;
    int x = 0;
    for (int i = 0; i < iters; i++) {
      x += uuidGenerator.generate().toString().length();
    }
    System.out.println(x);
    float secs = (System.currentTimeMillis() - xstartTime) / 1000.0f;
    System.out.println(uuidGenerator.getClass().getName() + " took " + secs + " secs, iters/sec=" + (iters / secs));
  }

}
