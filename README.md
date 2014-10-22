# Boundaries (Geofences) Sample app

The Boundaries sample app that introduces you to the geofence features of the ContextHub Android SDK and developer portal.

### Table of Contents

1. **[Purpose](#purpose)**
2. **[ContextHub Use Case](#contexthub-use-case)**
3. **[Background](#background)**
4. **[Getting Started](#getting-started)**
5. **[Create a Geofence](#create-a-geofence)**
6. **[Developer Portal](#developer-portal)**
7. **[Creating a New Context](#creating-a-new-context)**
8. **[Simulating Movement](#simulating-movement)**
8. **[ADB Logcat](#adb-logcat)**
10. **[Sample Code](#sample-code)**
11. **[Usage](#usage)**
  - **[Creating a Geofence](#creating-a-geofence)**
  - **[Retrieving Geofences by Tag](#retrieving-geofences-by-tag)**
  - **[Retrieving a Geofence by ID](#retrieving-a-geofence-by-id)**
  - **[Updating a Geofence](#updating-a-geofence)**
  - **[Deleting a Geofence](#deleting-a-geofence)**
  - **[Handling an Event](#handling-an-event)**
12. **[Final Words](#final-words)**

## Purpose
This sample application will show you how to create, retrieve, update, and delete (CRUD) geofences as well as respond to geofence in and out events in ContextHub.

## ContextHub Use Case
In this sample application, we use ContextHub to interact with geofences we are are of by registering them in the app with a tag so they autotomatically appear on every device registered with that same tag. ContextHub takes care of setting up and monitoring geofences automatically after creation and synchronization.

## Background

Geofences allow your application to wake up and respond to a user entering or exiting a particular location based on the WiFi and cellular triangulation constantly running on a user's device. This allows you to provide more contextual information through foreground notifications or run code for a brief period of time  so your app feels more responsive to the user at next launch. With contextual rules in ContextHub, this event processing can become more powerful allowing you to build even more contextually aware applications (see upcoming context rule sample app on how to write context rules)

## Getting Started

1. Get started by either forking or cloning the `boundaries-android` repo. Visit [GitHub Help](https://help.github.com/articles/fork-a-repo) if you need help.
2. Go to [ContextHub](http://app.contexthub.com) and create a new Boundaries application.
3. Find the app id associated with the application you just created. Its format looks something like this: `13e7e6b4-9f33-4e97-b11c-79ed1470fc1d`.
4. Open up the project and put the app id into the `ContextHub.init(this, "YOUR-APP-ID-HERE")` method call in the `BoundariesApp` class.
5. In the src/debug/res/values/google_maps_api.xml resource file, replace YOUR-API-KEY-HERE with your Google Maps API key. See the comments in that file for the help creating the API key.
6. On a physical Android device (Google Play Location Services tends to work best on a real device), go to Settings > Developer Options and check the "Allow mock locations" setting.
7. Build and run the project on your device.

## Create a Geofence

1. In the app, you'll see a map view that is centered at the default location (ChaiOne's office in Houston, TX).
2. Simulate a location change in the device by tapping somewhere on the map.
3. Tap the "+" at the top right of the screen to create a new geofence.  Give it a name and tap OK.  This will create a geofence on the ContextHub server.

## Developer Portal

1. Go to the [developer portal](https://app.contexthub.com) and click on your Boundaries app to access its data.
2. Click on the "Geofences" tab.  You should see the geofence that you just created in the list.  From here you can create, update, and delete geofences as well as change their tags.
3. Try to make a new geofence that's located close to where you are. Relaunch the app to see the geofence appear on your device.

## Creating a New Context

1. Contexts let you change how the server will respond to events triggered by devices. The real power of ContextHub comes from collecting and reacting these events to perform complex actions. Let's create a new context.
2. Click on "Contexts" tab, then click the "New Context" button to start making a new context rule.
3. Enter a name for this context which will be easy for you to remember. For now, name it "Geofence In".
4. Select the `"geofence_in"` event type. Now any event of type `"geofence_in"` will trigger this rule. You can have multiple rules with the same event type, which is why the name of events should be descriptive of the rule.
5. The Context Rule text box is where you can write a rule telling ContextHub what actions to take in response to an event triggered with the specific event type. This code is Javascript, and you have access to some context objects: event, push, vault, http, and console. For now, leave the Code box blank and then click save.
6. Create `"geofence_out"` rule as well in the portal. A rule must exist in ContextHub.com before a device will generate that specific event type automatically, so this is necessary to get those type of events to fire as well.

## Simulating Movement

1. Relaunch the app to load the updated context rules.
2. Simulate a location change in the device by tapping somewhere outside the geofence on the map.
3. Tap inside the geofence to change the simulated location back to where you previously were. You should see a geofence_in event logged to the console.
4. In the ContextHub [developer portal](http://app.contexthub.com), you should be able to see events generated by your device appear under "Latest events", no extra code needed!

## ADB Logcat

1. The sample app will log events into the debug console to get an idea of the JSON structure posted to ContextHub.
2. Use the `ContextHub.getInstance().addContextEventListener(ContextEventListener listener)` method to hook into the events generated by ContextHub.
3. Check out the ContextHub [documentation](http://docs.contexthub.com) for more information about the event pipeline and what you can do with it.

## Sample Code

In this sample, most of the important code that deals with CRUDing geofences occurs in `GeofenceListFragment`, `GeofenceCreateDialogFragment`, and `GeofenceUpdateDialogFragment`. Each create/update/delete method goes though a single operation you'll need to use the `GeofenceProxy` class. After each operation, a synchronization call is made so that `LocationService` is up to date with the latest data. This method becomes unnecessary if you have properly implemented push, as background notifications will take care of synchronization for you.

In addition, `GeofencesMapFragment` responds to any events created from the sensor pipeline through the `SensorPipelineListener.onEventReceived()` callback. At that point, you'll be able to filter whether the event was a geofence event which you were interested in and respond accordingly. There are several pre-defined keys that let you access information stored in an event, such as event name, state, type, etc..

## Usage

##### Creating a Geofence
```java
// Creating a geofence with name "Geofence", lat/lng of ChaiOne, tag "geofence-tag", radius 250 meters
GeofenceProxy proxy = new GeofenceProxy();
proxy.createGeofence("Geofence", 29.763638f, -95.461874f, 250, new String[]{"geofence-tag"},
        new Callback<Geofence> {
            @Override
            public void onSuccess(Geofence result) {
                // If you do not have push properly set up, you need to explicitly call synchronize on LocationService so it will generate events for this geofence
                LocationService.getInstance().synchronize();
                Toast.makeText(getActivity(), "Created geofence: " + result.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
```

##### Retrieving Geofences by Tag
```java
// Getting the first 10 geofences with the tag "geofence-tag" near our location in 2000 meter radius
GeofenceProxy proxy = new GeofenceProxy();
proxy.listGeofences(29.763638f, -95.461874f, 2000, 10, new String[]{"geofence-tag"},
        new Callback<List<Geofence>> {
            @Override
            public void onSuccess(List<Geofence> result) {
                for(Geofence geofence : result) {
                    Log.d(TAG, geofence.toString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.getMessage());
            }
        });
```

##### Retrieving a Geofence by ID
```java
// Getting a geofence with a specific ID
GeofenceProxy proxy = new GeofenceProxy();
proxy.getGeofence(1000, new Callback<Geofence> {
            @Override
            public void onSuccess(Geofence result) {
                Log.d(TAG, geofence.toString());
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.getMessage());
            }
        });
```

##### Updating a Geofence
```java
// Updating a geofence with the name "Geofence 2" and adding the tag "office"
// In order to update a geofence, you need to pass in a valid geofence object
geofence.setName("Geofence 2");
geofence.getTags().add("office");
GeofenceProxy proxy = new GeofenceProxy();
proxy.updateGeofence(geofence.getId(), geofence, new Callback<Geofence> {
            @Override
            public void onSuccess(Geofence result) {
                // If you do not have push properly set up, you need to explicitly call synchronize on LocationService so it will generate events for this geofence
                LocationService.getInstance().synchronize();
                Toast.makeText(getActivity(), result.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
```

##### Deleting a Geofence
```java
final long id = 1000;
GeofenceProxy proxy = new GeofenceProxy();
proxy.deleteGeofence(id, new Callback<Object>() {
            @Override
            public void onSuccess(Object result) {
                // If you do not have push properly set up, you need to explicitly call synchronize on LocationService so it will stop generating events for this geofence
                LocationService.getInstance().synchronize();
                Log.d(TAG, String.format("Successfully deleted geofence id %s", id));
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.getMessage());
            }
        });
```

##### Handling an Event
```java
@Override
public void onResume() {
    super.onResume();

    // start listening to events
    ContextHub.getInstance().addSensorPipelineListener(this);
}

@Override
public void onPause() {
    super.onPause();

    // stop listening to events
    ContextHub.getInstance().removeSensorPipelineListener(this);
}

@Override
public void onEventReceived(final SensorPipelineEvent event) {
    if(event.getName().startsWith("geofence_")) {
        // called on background thread, so use a Runnable to perform work on UI thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), event.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

@Override
public boolean shouldPostEvent(SensorPipelineEvent event) {
    // return true to allow events to post, false to prevent them from posting
    return true;
}

@Override
public void onBeforeEventPosted(SensorPipelineEvent event) {
    // add any extra details to the event before it is posted
}

@Override
public void onEventPosted(SensorPipelineEvent event) {
    // handle an event after it has been posted to ContextHub
}
```

## Final Words

That's it! Hopefully this sample application showed you how easy it is to work with geofences in ContextHub to present contextual information based on location.