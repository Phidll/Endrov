///////////////////////////////////////////////////////////////////////////////
//FILE:          Setting.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, October 29, 2006
//
// COPYRIGHT:    University of California, San Francisco, 2006
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// CVS:          $Id: Setting.java,v 1.1 2009-08-11 15:47:35 mahogny Exp $
//
package endrov.driverMicromanager.conf;

/**
 * Data structure for device settings.
 *
 */
public class Setting {
   public String deviceName_;
   public String propertyName_;
   public String propertyValue_;
   
   public Setting() {
      deviceName_ = new String("NoName");
      propertyName_ = new String("Undefined");
      propertyValue_ = new String();
   }
   
   public Setting(String devName, String propName, String propVal) {
      deviceName_ = devName;
      propertyName_ = propName;
      propertyValue_ = propVal;
   }
   
   /**
    * Comapres two settings based on their content.
    */
   public boolean isEqualTo(Setting s) {
      if (deviceName_.compareTo(s.deviceName_) == 0 &&
          propertyName_.compareTo(s.propertyName_) == 0 &&
          propertyValue_.compareTo(s.propertyValue_) == 0) {
         return true;
      }
      return false;
   }
   
   /**
    * Two settings match if deviceName and propertyName are the same. The value
    * is not taken into account.
    */
   public boolean matches(Setting s) {
      if (deviceName_.compareTo(s.deviceName_) == 0 &&
            propertyName_.compareTo(s.propertyName_) == 0) {
           return true;
      }
      return false;
   }
   
   public String toString() {
      return new String(deviceName_ + ":" + propertyName_ + "=" + propertyValue_);
   }
}