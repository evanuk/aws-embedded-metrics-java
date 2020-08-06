/*
 *   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package software.amazon.awssdk.services.cloudwatchlogs.emf.environment;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.emf.Constants;
import software.amazon.awssdk.services.cloudwatchlogs.emf.config.Configuration;
import software.amazon.awssdk.services.cloudwatchlogs.emf.sinks.AgentSink;
import software.amazon.awssdk.services.cloudwatchlogs.emf.sinks.Endpoint;
import software.amazon.awssdk.services.cloudwatchlogs.emf.sinks.ISink;
import software.amazon.awssdk.services.cloudwatchlogs.emf.sinks.SocketClientFactory;

@Slf4j
public abstract class AgentBasedEnvironment implements Environment {
    private Configuration config;
    private ISink sink;

    public AgentBasedEnvironment(Configuration config) {
        this.config = config;
    }

    @Override
    public String getName() {
        if (!config.getServiceName().isPresent()) {
            log.warn("Unknown ServiceName.");
            return Constants.UNKNOWN;
        }
        return config.getServiceName().get();
    }

    @Override
    public String getLogGroupName() {
        return config.getLogGroupName().orElse(getName() + "-metrics");
    }

    public String getLogStreamName() {
        return config.getLogStreamName().orElse(getName() + "-stream");
    }

    @Override
    public ISink getSink() {
        if (sink == null) {
            Endpoint endpoint;
            if (!config.getAgentEndpoint().isPresent()) {
                log.info(
                        "Endpoint is not defined. Using default: {}",
                        Endpoint.DEFAULT_TCP_ENDPOINT);
                endpoint = Endpoint.DEFAULT_TCP_ENDPOINT;
            } else {
                endpoint = Endpoint.fromURL(config.getAgentEndpoint().get());
            }
            sink =
                    new AgentSink(
                            getLogGroupName(),
                            getLogStreamName(),
                            endpoint,
                            new SocketClientFactory());
        }
        return sink;
    }
}
