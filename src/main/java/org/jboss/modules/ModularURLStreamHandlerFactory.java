/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.modules;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The root URL stream handler factory for the module system.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class ModularURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final PrivilegedAction<String> URL_MODULES_LIST_ACTION = new PropertyReadAction("jboss.protocol.handler.modules");

    ModularURLStreamHandlerFactory() {
    }

    public URLStreamHandler createURLStreamHandler(final String protocol) {
        final SecurityManager sm = System.getSecurityManager();
        final String urlModulesList;
        if (sm != null) {
            urlModulesList = AccessController.doPrivileged(URL_MODULES_LIST_ACTION);
        } else {
            urlModulesList = URL_MODULES_LIST_ACTION.run();
        }
        if (urlModulesList == null) {
            return null;
        }
        int f = 0;
        int i;
        do {
            i = urlModulesList.indexOf('|', f);
            final String moduleId = (i == -1 ? urlModulesList.substring(f) : urlModulesList.substring(f, i)).trim();
            if (moduleId.length() > 0) {
                try {
                    final ModuleIdentifier identifier = ModuleIdentifier.fromString(moduleId);
                    final ServiceLoader<URLStreamHandlerFactory> loader = Module.getModuleFromDefaultLoader(identifier).loadService(URLStreamHandlerFactory.class);
                    for (URLStreamHandlerFactory factory : loader) {
                        final URLStreamHandler handler = factory.createURLStreamHandler(protocol);
                        if (handler != null) {
                            return handler;
                        }
                    }
                } catch (RuntimeException e) {
                    // skip it
                } catch (ModuleLoadException e) {
                    // skip it
                }
            }
            f = i + 1;
        } while (i != -1);
        return null;
    }

    private static final AtomicReference<Map<String, URLStreamHandler>> targets = new AtomicReference<Map<String, URLStreamHandler>>(Collections.<String, URLStreamHandler>emptyMap());

    private static final Method openConnection1;
    private static final Method openConnection2;
    private static final Method parseURL;
    private static final Method getDefaultPort;
    private static final Method equals;
    private static final Method hashCode;
    private static final Method sameFile;
    private static final Method getHostAddress;
    private static final Method hostsEqual;
    private static final Method toExternalForm;
    private static final Method setURL9;
    private static final Method setURL6;

    static {
        final Method[] methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
            public Method[] run() {
                final Method[] methods = new Method[12];
                try {
                    final Class<URLStreamHandler> c = URLStreamHandler.class;
                    methods[0] = c.getDeclaredMethod("openConnection", URL.class);
                    methods[1] = c.getDeclaredMethod("openConnection", URL.class, Proxy.class);
                    methods[2] = c.getDeclaredMethod("parseURL", URL.class, String.class, int.class, int.class);
                    methods[3] = c.getDeclaredMethod("getDefaultPort");
                    methods[4] = c.getDeclaredMethod("equals", URL.class, URL.class);
                    methods[5] = c.getDeclaredMethod("hashCode", URL.class);
                    methods[6] = c.getDeclaredMethod("sameFile", URL.class, URL.class);
                    methods[7] = c.getDeclaredMethod("getHostAddress", URL.class);
                    methods[8] = c.getDeclaredMethod("hostsEqual", URL.class, URL.class);
                    methods[9] = c.getDeclaredMethod("toExternalForm", URL.class);
                    methods[10] = c.getDeclaredMethod("setURL", URL.class, String.class, String.class, int.class, String.class, String.class, String.class, String.class, String.class);
                    methods[11] = c.getDeclaredMethod("setURL", URL.class, String.class, String.class, int.class, String.class, String.class);
                    for (Method method : methods) {
                        method.setAccessible(true);
                    }
                } catch (NoSuchMethodException e) {
                    throw new NoSuchMethodError(e.getMessage());
                }
                return methods;
            }
        });
        openConnection1 = methods[0];
        openConnection2 = methods[1];
        parseURL = methods[2];
        getDefaultPort = methods[3];
        equals = methods[4];
        hashCode = methods[5];
        sameFile = methods[6];
        getHostAddress = methods[7];
        hostsEqual = methods[8];
        toExternalForm = methods[9];
        setURL9 = methods[10];
        setURL6 = methods[11];
    }

    private static Object invoke(Method method, Object target, Object... params) {
        try {
            return method.invoke(target, params);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            try {
                throw e.getCause();
            } catch (Error e2) {
                throw e2;
            } catch (RuntimeException e2) {
                throw e2;
            } catch (Throwable throwable) {
                throw new UndeclaredThrowableException(throwable);
            }
        }
    }

    private static Object invokeEx(Method method, Object target, Object... params) throws IOException {
        try {
            return method.invoke(target, params);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            try {
                throw e.getCause();
            } catch (IOException e2) {
                throw e2;
            } catch (Error e2) {
                throw e2;
            } catch (RuntimeException e2) {
                throw e2;
            } catch (Throwable throwable) {
                throw new UndeclaredThrowableException(throwable);
            }
        }
    }

    static final class URLStreamHandlerImpl extends URLStreamHandler {
        private final String proto;

        URLStreamHandlerImpl(final String proto) {
            this.proto = proto;
        }

        protected URLConnection openConnection(final URL u) throws IOException {
            final URLStreamHandler target = targets.get(proto);
            if (target == null) {
                throw new MalformedURLException("Unknown URL protocol '" + proto + "'");
            }
            return (URLConnection) invokeEx(openConnection1, target, u);
        }

        protected URLConnection openConnection(final URL u, final Proxy p) throws IOException {
            final URLStreamHandler target = targets.get(proto);
            if (target == null) {
                throw new MalformedURLException("Unknown URL protocol '" + proto + "'");
            }
            return (URLConnection) invokeEx(openConnection2, target, u, p);
        }

        protected void parseURL(final URL u, final String spec, final int start, final int limit) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            invoke(parseURL, target, u, spec, Integer.valueOf(start), Integer.valueOf(limit));
        }

        protected int getDefaultPort() {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            return ((Integer) invoke(getDefaultPort, target)).intValue();
        }

        protected boolean equals(final URL u1, final URL u2) {
            final URLStreamHandler target = targets.get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            return invoke(equals, target, u1, u2) == Boolean.TRUE;
        }

        protected int hashCode(final URL u) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            return ((Integer) invoke(hashCode, target, u)).intValue();
        }

        protected boolean sameFile(final URL u1, final URL u2) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            return invoke(sameFile, target, u1, u2) == Boolean.TRUE;
        }

        protected InetAddress getHostAddress(final URL u) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            return (InetAddress) invoke(getHostAddress, target, u);
        }

        protected boolean hostsEqual(final URL u1, final URL u2) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            return invoke(hostsEqual, target, u1, u2) == Boolean.TRUE;
        }

        protected String toExternalForm(final URL u) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            return (String) invoke(toExternalForm, target, u);
        }

        protected void setURL(final URL u, final String protocol, final String host, final int port, final String authority, final String userInfo, final String path, final String query, final String ref) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            invoke(setURL9, target, u, protocol, host, Integer.valueOf(port), authority, userInfo, path, query, ref);
        }

        @Deprecated
        protected void setURL(final URL u, final String protocol, final String host, final int port, final String file, final String ref) {
            final URLStreamHandler target = targets.get().get(proto);
            if (target == null) {
                throw new RuntimeException(new MalformedURLException("Unknown URL protocol '" + proto + "'"));
            }
            invoke(setURL6, target, u, protocol, host, Integer.valueOf(port), ref);
        }
    }
}
