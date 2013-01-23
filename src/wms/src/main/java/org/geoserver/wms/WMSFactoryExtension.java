/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms;

import org.geoserver.config.ServiceFactoryExtension;


public class WMSFactoryExtension extends ServiceFactoryExtension<WMSInfo> {

    public WMSFactoryExtension() {
        super(WMSInfo.class);
    }

    @Override
    public <T> T create(Class<T> clazz) {
        return (T) new WMSInfoImpl();
    }

}
