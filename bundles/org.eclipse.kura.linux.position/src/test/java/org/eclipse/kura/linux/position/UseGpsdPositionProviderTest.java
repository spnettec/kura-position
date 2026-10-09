/*******************************************************************************
 * Copyright (c) 2011, 2025 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.linux.position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.linux.position.gpsd.GpsdPositionProvider;
import org.eclipse.kura.linux.position.options.PositionServiceOptions;
import org.eclipse.kura.linux.position.provider.PositionProviderType;
import org.eclipse.kura.position.GNSSType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.osgi.util.measurement.Measurement;
import org.osgi.util.measurement.Unit;
import org.osgi.util.position.Position;

import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.IGPSObject;
import de.taimos.gpsd4java.types.ParseException;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;

public class UseGpsdPositionProviderTest {

    private GpsdPositionProvider gpsdPositionProvider;
    private GPSdEndpoint gpsEndpointMock;

    @AfterEach
    void stopProvider() {
        if (this.gpsdPositionProvider != null) {
            this.gpsdPositionProvider.stop();
        }
    }
    private ResultParser parser = new ResultParser();

    private final String DEVICE_2_1_JSON_STREAM = "gpsd-raw-json-device-2-1.txt";
    private final String BOLTGATE_10_12_JSON_STREAM_2 = "gpsd-raw-json-device-2-2.txt";
    private final String DEVICE1_JSON_STREAM = "gpsd-raw-json-device-1.txt";
    private final String MULTI_DEVICE_JSON_STREAM = "gpsd-raw-json-multi-device-1.txt";
    private final String MULTI_DEVICE_JSON_STREAM_2 = "gpsd-raw-json-multi-device-2.txt";

    @Test
    public void startGpsdPositionProvider() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());

        whenGpsdProviderIsStarted();

        thenIsStartedProperly();
    }

    @Test
    public void stopGpsdPositionProvider() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());

        whenGpsdProviderIsStopped();

        thenIsStoppedProperly();
    }

    @Test
    public void getPositionFromDevice2Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE_2_1_JSON_STREAM);

        thenPositionIsNotNull();
        thenGnssTypeIs(new HashSet<>(Arrays.asList(GNSSType.GPS, GNSSType.GLONASS)));
    }

    @Test
    public void getPositionFromDevice2Stream2() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(BOLTGATE_10_12_JSON_STREAM_2);

        thenPositionIsNotNull();
    }

    @Test
    public void getPositionFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE1_JSON_STREAM);

        thenPositionIsNotNull();
    }

    @Test
    public void verifyPositionIsLockedFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE1_JSON_STREAM);

        thenPositionIsLocked();
    }

    @Test
    public void getNmeaPositionFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE1_JSON_STREAM);

        thenNmeaPositionIsNotNull();
    }

    @Test
    public void getNmeaDateFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE1_JSON_STREAM);

        thenNmeaDateIsNotAvailable();
    }

    @Test
    public void getNmeaTimeFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE1_JSON_STREAM);

        thenNmeaTimeIsNotAvailable();
    }

    @Test
    public void getDateTimeFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE1_JSON_STREAM);

        thenDateTimeIsNotNull();
    }

    @Test
    public void verifyPositionIsZeroFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        thenPositionIsZero();
    }

    @Test
    public void getGnssTypeFromDevice1Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE1_JSON_STREAM);

        thenGnssTypeIs(new HashSet<>(Arrays.asList(GNSSType.GPS)));
    }

    @Test
    public void getGnssTypeFromDevice2Stream() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(DEVICE_2_1_JSON_STREAM);

        thenGnssTypeIs(new HashSet<>(Arrays.asList(GNSSType.GPS, GNSSType.GLONASS)));
    }

    @Test
    public void getGnssTypeFromDevice2Stream2() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(BOLTGATE_10_12_JSON_STREAM_2);

        thenGnssTypeIs(new HashSet<>(Arrays.asList(GNSSType.GPS, GNSSType.GLONASS)));
    }

    @Test
    public void shouldGetPositionFromGpsWithFix() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(MULTI_DEVICE_JSON_STREAM);

        thenPositionIsNotNull();
        thenPositionIs(41.828250000, 12.268816667, 0.0);
    }

    @Test
    public void shouldNotGetPositionIfNoFix() {
        givenGpsdPositionProvider();
        givenProperties(defaultProperties());
        givenGpsdProviderIsStarted();

        whenNMEAStreamArriveFrom(MULTI_DEVICE_JSON_STREAM_2);

        thenPositionHasZeroValues();
    }

    private void givenGpsdPositionProvider() {
        this.gpsdPositionProvider = new GpsdPositionProvider();
    }

    private void givenProperties(Map<String, Object> properties) {
        this.gpsdPositionProvider.init(createOptionsFromProperties(properties), null, null);
        this.gpsEndpointMock = mock(GPSdEndpoint.class);
        try {
            TestUtil.setFieldValue(this.gpsdPositionProvider, "gpsEndpoint", this.gpsEndpointMock);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Failed to inject the mocked GPSd endpoint", e);
        }
    }

    private void givenGpsdProviderIsStarted() {
        gpsdPositionProviderStart();
    }

    private void whenNMEAStreamArriveFrom(String filename) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                java.util.Objects.requireNonNull(getClass().getResourceAsStream("/" + filename)),
                StandardCharsets.UTF_8))) {
            reader.lines().forEach(jsonString -> {
                try {
                    IGPSObject gpsObject = this.parser.parse(jsonString);
                    if (gpsObject instanceof TPVObject) {
                        this.gpsdPositionProvider.handleTPV((TPVObject) gpsObject);
                    }
                    if (gpsObject instanceof SKYObject) {
                        this.gpsdPositionProvider.handleSKY((SKYObject) gpsObject);
                    }
                } catch (ParseException e) {
                    throw new AssertionError("Invalid GPSd fixture: " + filename, e);
                }
            });
        } catch (IOException e) {
            throw new AssertionError("Failed to read GPSd fixture: " + filename, e);
        }

    }

    private void whenGpsdProviderIsStarted() {
        gpsdPositionProviderStart();
    }

    private void whenGpsdProviderIsStopped() {
        this.gpsdPositionProvider.stop();
    }

    private void thenIsStartedProperly() {
        verify(this.gpsEndpointMock).start();
    }

    private void thenIsStoppedProperly() {
        verify(this.gpsEndpointMock).stop();
    }

    private void thenPositionIsNotNull() {
        assertNotNull(this.gpsdPositionProvider.getPosition());
    }

    private void thenPositionIs(double latitude, double longitude, double altitude) {
        Position position = this.gpsdPositionProvider.getPosition();
        assertEquals(latitude, position.getLatitude().getValue() * 180/Math.PI, 0.000001);
        assertEquals(longitude, position.getLongitude().getValue() * 180/Math.PI, 0.000001);
        assertEquals(altitude, position.getAltitude().getValue(), 0.0001);
    }

    private void thenPositionHasZeroValues() {
        thenPositionIs(0.0, 0.0, 0.0);
    }

    private void thenNmeaDateIsNotAvailable() {
        try {
            this.gpsdPositionProvider.getNmeaDate();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    private void thenNmeaTimeIsNotAvailable() {
        try {
            this.gpsdPositionProvider.getNmeaTime();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    private void thenNmeaPositionIsNotNull() {
        assertNotNull(this.gpsdPositionProvider.getNmeaPosition());
    }

    private void thenPositionIsZero() {
        Measurement zeroMesRad = new Measurement(0d, 0d, Unit.rad);
        Measurement zeroMesMeters = new Measurement(0d, 0d, Unit.m);
        Measurement zeroMesMetersSecond = new Measurement(0d, 0d, Unit.m_s);

        Position zeroPosition = new Position(zeroMesRad, zeroMesRad, zeroMesMeters, zeroMesMetersSecond, zeroMesRad);
        Position position = this.gpsdPositionProvider.getPosition();

        assertEquals(zeroPosition.getLongitude(), position.getLongitude());
        assertEquals(zeroPosition.getAltitude(), position.getAltitude());
        assertEquals(zeroPosition.getLatitude(), position.getLatitude());
        assertEquals(zeroPosition.getSpeed(), position.getSpeed());
        assertEquals(zeroPosition.getTrack(), position.getTrack());
    }

    private void thenDateTimeIsNotNull() {
        assertNotNull(this.gpsdPositionProvider.getDateTime());
    }

    private void thenPositionIsLocked() {
        assertTrue(this.gpsdPositionProvider.isLocked());
    }

    private void thenGnssTypeIs(Set<GNSSType> types) {
        assertEquals(types, this.gpsdPositionProvider.getGnssTypes());
    }

    private void gpsdPositionProviderStart() {
        this.gpsdPositionProvider.start();
    }

    private PositionServiceOptions createOptionsFromProperties(Map<String, Object> properties) {
        return new PositionServiceOptions(properties);
    }

    private Map<String, Object> defaultProperties() {
        Map<String, Object> propertiesMap = new HashMap<String, Object>();
        propertiesMap.put("enabled", true);
        propertiesMap.put("provider", PositionProviderType.GPSD.getValue());
        propertiesMap.put("gpsd.host", "localhost");
        propertiesMap.put("gpsd.port", 2947);
        return propertiesMap;
    }

}
