/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.modules.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Static factory methods for class name filter types.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ClassNameFilters {
    private ClassNameFilters() {}

    /**
     * Get a class name filter which returns {@code true} if all of the given filters return {@code true}.
     *
     * @param filters the filters
     * @return the "all" filter
     */
    public static ClassNameFilter all(ClassNameFilter... filters) {
        return new AggregateClassNameFilter(false, filters);
    }

    /**
     * Get a class name filter which returns {@code true} if all of the given filters return {@code true}.
     *
     * @param filters the filters
     * @return the "all" filter
     */
    public static ClassNameFilter all(Collection<ClassNameFilter> filters) {
        return all(filters.toArray(new ClassNameFilter[filters.size()]));
    }

    /**
     * Get a class name filter which returns {@code true} if any of the given filters return {@code true}.
     *
     * @param filters the filters
     * @return the "any" filter
     */
    public static ClassNameFilter any(ClassNameFilter... filters) {
        return new AggregateClassNameFilter(true, filters);
    }

    /**
     * Get a class name filter which returns {@code true} if any of the given filters return {@code true}.
     *
     * @param filters the filters
     * @return the "any" filter
     */
    public static ClassNameFilter any(Collection<ClassNameFilter> filters) {
        return any(filters.toArray(new ClassNameFilter[filters.size()]));
    }

    /**
     * Get a class name filter which is {@code true} when the given filter is {@code false}, and vice-versa.
     *
     * @param filter the filter
     * @return the inverting filter
     */
    public static ClassNameFilter not(ClassNameFilter filter) {
        return new InvertingClassNameFilter(filter);
    }

    /**
     * Get a class name filter which matches an exact class name.
     *
     * @param className the class name
     * @return a filter which returns {@code true} if the class name is an exact match
     */
    public static ClassNameFilter is(String className) {
        return new EqualsClassNameFilter(className);
    }

    /**
     * Get a builder for a multiple-class-name filter.  Such a filter contains multiple filters, each associated
     * with a flag which indicates that matching class names should be included or excluded.
     *
     * @param defaultValue the value to return if none of the nested filters match
     * @return the builder
     */
    public static MultipleClassNameFilterBuilder multipleClassNameFilterBuilder(boolean defaultValue) {
        return new MultipleClassNameFilterBuilder(defaultValue);
    }

    /**
     * Get a filter which always returns {@code true}.
     *
     * @return the accept-all filter
     */
    public static ClassNameFilter acceptAll() {
        return BooleanClassNameFilter.TRUE;
    }

    /**
     * Get a filter which always returns {@code false}.
     *
     * @return the reject-all filter
     */
    public static ClassNameFilter rejectAll() {
        return BooleanClassNameFilter.FALSE;
    }

    /**
     * Get a filter which returns {@code true} if the tested class name is contained within the given set.
     * Each member of the set is a class name separated by "{@code /}" characters; {@code null}s are disallowed.
     *
     * @param classNames the class name set
     * @return the filter
     */
    public static ClassNameFilter in(Set<String> classNames) {
        return new SetClassNameFilter(new HashSet<String>(classNames));
    }
}
