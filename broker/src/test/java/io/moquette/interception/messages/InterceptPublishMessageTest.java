/*
 * Copyright (c) 2012-2026 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.moquette.interception.messages;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterceptPublishMessageTest {

    @Test
    public void exposesMqttPropertiesOfTheInterceptedPublish() {
        MqttProperties properties = new MqttProperties();
        properties.add(new MqttProperties.StringProperty(
            MqttProperties.MqttPropertyType.CONTENT_TYPE.value(), "application/json"));
        properties.add(new MqttProperties.StringProperty(
            MqttProperties.MqttPropertyType.RESPONSE_TOPIC.value(), "response/topic"));
        properties.add(new MqttProperties.UserProperty("origin", "sensor-42"));

        MqttPublishMessage publish = MqttMessageBuilders.publish()
            .topicName("telemetry/temperature")
            .qos(MqttQoS.AT_MOST_ONCE)
            .payload(Unpooled.EMPTY_BUFFER)
            .properties(properties)
            .build();

        InterceptPublishMessage intercepted = new InterceptPublishMessage(publish, "cli1234", "usr1234");

        MqttProperties exposed = intercepted.getProperties();
        MqttProperties.MqttProperty contentType =
            exposed.getProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value());
        assertEquals("application/json", ((MqttProperties.StringProperty) contentType).value());

        MqttProperties.MqttProperty responseTopic =
            exposed.getProperty(MqttProperties.MqttPropertyType.RESPONSE_TOPIC.value());
        assertEquals("response/topic", ((MqttProperties.StringProperty) responseTopic).value());

        List<? extends MqttProperties.MqttProperty> userProperties =
            exposed.getProperties(MqttProperties.MqttPropertyType.USER_PROPERTY.value());
        assertEquals(1, userProperties.size());
        MqttProperties.UserProperty userProperty = (MqttProperties.UserProperty) userProperties.get(0);
        assertEquals("origin", userProperty.value().key);
        assertEquals("sensor-42", userProperty.value().value);
    }
}
