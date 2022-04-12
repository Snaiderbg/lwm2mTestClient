package com.kenkoclient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.model.InvalidDDFFileException;
import org.eclipse.leshan.core.model.InvalidModelException;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.util.NamedThreadFactory;
import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;

public class KenkoClient {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        String[] endpoint = { "kenko:test1", "kenko:test2", "kenko:test3", "kenko:test4", "kenko:test5" };
        ScheduledExecutorService sharedExecutor = Executors.newScheduledThreadPool(500,
                new NamedThreadFactory("shared executor"));

        LeshanClient[] clients = new LeshanClient[endpoint.length];
        for (int i = 0; i < clients.length; i++) {
            LeshanClientBuilder builder = new LeshanClientBuilder(endpoint[i]);
            builder.setSharedExecutor(sharedExecutor);

            List<ObjectModel> models = ObjectLoader.loadDefault();
            String[] modelPaths = new String[] { "10246.xml", "3303.xml" };
            try {
                models.addAll(ObjectLoader.loadDdfResources("/models", modelPaths));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidModelException e) {
                e.printStackTrace();
            } catch (InvalidDDFFileException e) {
                e.printStackTrace();
            }

            ObjectsInitializer initializer = new ObjectsInitializer(new StaticModel(models));

            String sf0 = String.format("Kenko%s", i);
            String sf1 = String.format("Test%s", i);
            // create instances
            // initializer.setInstancesForObject(LwM2mId.SECURITY,Security.noSec("coap://leshan.eclipseprojects.io:5683",
            // 12345));
            initializer.setInstancesForObject(LwM2mId.SECURITY,
                    Security.noSec("coap://demodm.friendly-tech.com:5683", 12345));
            initializer.setInstancesForObject(LwM2mId.SERVER, new Server(12345, 5 * 60));
            initializer.setInstancesForObject(LwM2mId.DEVICE, new Device(sf0, sf1, "08088224466"));
            initializer.setInstancesForObject(LwM2mId.LOCATION, new Location(90f, 180f, 1.0f));
            initializer.setInstancesForObject(3303, new RandomTemperatureSensor());

            List<LwM2mObjectEnabler> enablers = initializer.createAll();
            builder.setObjects(enablers);
            clients[i] = builder.build();
            clients[i].start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
