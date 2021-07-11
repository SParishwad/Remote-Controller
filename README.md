# Remote Controller

This code is a part of a larger project. The Remote Controller app shall be used to transmit the Joystick signals to the NodeMCU. 
The Android is connected to the WiFi Access point created by the NodeMCU. 
 
The Remote Controller app is a modified version of the Joystick app by [erz05](https://github.com/erz05/JoyStick). 
The modifications include the use of WebSockets to transmit the Joystick signals to the NodeMCU so that it can be used either directly to control 
servos on a remote controlled vehicle or as in this case, to forward the signals via Radio communication to the Plane. 
Another modification includes a haptic feedback provided by the phone with the help of vibrations when the joystick is moved. 
Also the JoyStickListener provides the x and y percentages of the joystick movement. 

<H2>Sample App</H2>
<img height="70px" src="https://github.com/erz05/JoyStick/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" />

<img width="500px" src="https://github.com/SParishwad/Remote-Controller/blob/main/images/WhatsApp%20Image%202021-07-11%20at%2022.05.36.jpeg" />
<br><br>
<img width="500px" src="https://github.com/SParishwad/Remote-Controller/blob/main/images/WhatsApp%20Image%202021-07-11%20at%2022.06.07.jpeg" />
<br><br>

<H2>Usage</H2>

<H2>JoyStickListener:</H2>

```java
//JoyStickListener Interface
public interface JoyStickListener {
        void onMove(JoyStick joyStick, float xPercent, float yPercent, double angle);
}

//Set JoyStickListener
joyStick.setListener(this);
```
onMove: gets called everytime theres a touch interaction


<H2>JavaClass GameView:</H2>
This public class extends the surface view and 

<H2>WebSockets</H2>
The WebSockets library by [Takahiko Kawasaki](https://github.com/TakahikoKawasaki/nv-websocket-client) is used to create a WebSockets client. 
A Websocket connection is created when the surface is created and the handler is responsible to transmit the control signals every 150 ms through a runnable object. 

<H2>Axis Types:</H2>
1. TYPE_8_AXIS 
2. TYPE_4_AXIS 
3. TYPE_2_AXIS_LEFT_RIGHT 
4. TYPE_2_AXIS_UP_DOWN

To set Axis Type:

```java
joyStick.setType(JoyStick.TYPE_4_AXIS);
```

<H2>Getters/Setters</H2>

```java
//Set GamePad Color
joyStick.setPadColor(Color.BLACK);

//Set Button Color
joyStick.setButtonColor(Color.RED);

//Set Background Image
joyStick.setPadBackground(resId);

//Set Button Image
joyStick.setButtonDrawable(resId);

//Set Button Scale
joyStick.setButtonRadiusScale(scale);

//Enable Button to Stay Put
joyStick.enableStayPut(enable);

//Get Power
joyStick.getPower();

//Get Angle
joyStick.getAngle();

//Get Angle in Degrees
joyStick.getAngleDegrees();
```

<H2>License</H2>
    Copyright 2015 erz05

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
