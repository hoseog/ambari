/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.api.resources;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.ambari.server.api.services.Request;
import org.apache.ambari.server.api.util.TreeNode;
import org.apache.ambari.server.controller.internal.RequestResourceProvider;
import org.apache.ambari.server.controller.spi.Resource;


/**
 * Request resource definition.
 */
public class RequestResourceDefinition extends BaseResourceDefinition {

  /**
   * Constructor.
   */
  public RequestResourceDefinition() {
    super(Resource.Type.Request);
  }

  @Override
  public String getPluralName() {
    return "requests";
  }

  @Override
  public String getSingularName() {
    return "request";
  }

  @Override
  public Set<SubResourceDefinition> getSubResourceDefinitions() {
      return Collections.singleton(new SubResourceDefinition(Resource.Type.Task));
  }

  @Override
  public List<PostProcessor> getPostProcessors() {
    return Arrays.asList(new RequestHrefPostProcessor(), new RequestSourceScheduleHrefPostProcessor());
  }

  private class RequestHrefPostProcessor implements PostProcessor {
    @Override
    public void process(Request request, TreeNode<Resource> resultNode,
        String href) {
      Object requestId = resultNode.getObject().getPropertyValue(
          getClusterController().getSchema(Resource.Type.Request)
              .getKeyPropertyId(Resource.Type.Request));

      // sanity check for a trailing slash since this would cause problems
      // with string-based URL matching
      if (href.endsWith("/")) {
        href = href.substring(0, href.length() - 1);
      }

      // if the original href was for a "request", then shortcut and just
      // append the ID onto the URL
      StringBuilder sb = new StringBuilder();
      if (href.endsWith("/requests")) {
        sb.append(href);
        sb.append('/').append(requestId);
      } else {
        // split the href up into its parts, intercepting "clusers" in order
        // to rewrite the href to be scoped for requests
        String[] tokens = href.split("/");

        for (int i = 0; i < tokens.length; ++i) {
          String fragment = tokens[i];
          sb.append(fragment).append('/');

          if ("clusters".equals(fragment) && i + 1 < tokens.length) {
            String clusterName = tokens[i + 1];
            sb.append(clusterName).append("/");
            sb.append("requests/").append(requestId);
            break;
          }
        }
      }

      resultNode.setProperty("href", sb.toString());
    }
  }

  private class RequestSourceScheduleHrefPostProcessor implements PostProcessor {

    @Override
    public void process(Request request, TreeNode<Resource> resultNode, String href) {
      StringBuilder sb = new StringBuilder();
      String[] toks = href.split("/");

      for (int i = 0; i < toks.length; ++i) {
        String s = toks[i];
        sb.append(s).append('/');
        if ("clusters".equals(s)) {
          sb.append(toks[i + 1]).append('/');
          break;
        }
      }

      Object scheduleId = resultNode.getObject().getPropertyValue(RequestResourceProvider.REQUEST_SOURCE_SCHEDULE_ID);
      if (scheduleId != null) {
        sb.append("request_schedules/").append(scheduleId);
        resultNode.getObject().setProperty(RequestResourceProvider.REQUEST_SOURCE_SCHEDULE_HREF, sb.toString());
      }
    }
  }
}
