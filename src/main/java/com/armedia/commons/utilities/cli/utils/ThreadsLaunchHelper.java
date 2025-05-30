/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 *
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities.cli.utils;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.cli.Option;
import com.armedia.commons.utilities.cli.OptionGroup;
import com.armedia.commons.utilities.cli.OptionGroupImpl;
import com.armedia.commons.utilities.cli.OptionImpl;
import com.armedia.commons.utilities.cli.OptionValues;
import com.armedia.commons.utilities.cli.Options;
import com.armedia.commons.utilities.cli.filter.IntegerValueFilter;

public final class ThreadsLaunchHelper extends Options
{

    public static final int DEFAULT_MIN_THREADS = 1;
    public static final int DEFAULT_DEF_THREADS = (Runtime.getRuntime().availableProcessors() * 2);
    public static final int DEFAULT_MAX_THREADS = (Runtime.getRuntime().availableProcessors() * 4);

    public static final Option THREADS = new OptionImpl() //
            .setShortOpt('t') //
            .setLongOpt("threads") //
            .setArgumentLimits(1) //
            .setDefault(String.valueOf(ThreadsLaunchHelper.DEFAULT_DEF_THREADS)) //
            .setArgumentName("threads") //
            .setDescription("The number of threads to use") //
            .setValueFilter(
                    new IntegerValueFilter(ThreadsLaunchHelper.DEFAULT_MIN_THREADS, ThreadsLaunchHelper.DEFAULT_MAX_THREADS)) //
    ;

    public static final Option UNCAP_THREADS = new OptionImpl() //
            .setLongOpt("uncap-threads") //
            .setDescription("Uncap the maximum number of threads (TREAD CAREFULLY!!)") //
    ;

    private final int min;
    private final Integer def;
    private final int max;

    private final OptionGroupImpl group;

    public ThreadsLaunchHelper()
    {
        this(ThreadsLaunchHelper.DEFAULT_MIN_THREADS, ThreadsLaunchHelper.DEFAULT_MAX_THREADS);
    }

    public ThreadsLaunchHelper(int max)
    {
        this(ThreadsLaunchHelper.DEFAULT_MIN_THREADS, max);
    }

    public ThreadsLaunchHelper(int min, int max)
    {
        this(min, null, max);
    }

    public ThreadsLaunchHelper(int min, Integer def, int max)
    {
        min = Math.max(1, min);
        if (max < min)
        {
            throw new IllegalArgumentException(
                    String.format("Maximum value %d is lower than minmum value %d", max, min));
        }
        this.min = min;
        this.max = max;
        this.def = ((def != null) ? Tools.ensureBetween(min, def, max) : null);
        this.group = new OptionGroupImpl("Threading") //
                .add(ThreadsLaunchHelper.THREADS) //
                .add(ThreadsLaunchHelper.UNCAP_THREADS) //
        ;
    }

    public int getMin()
    {
        return this.min;
    }

    public int getMax()
    {
        return this.max;
    }

    public boolean hasDefault()
    {
        return (this.def != null);
    }

    public Integer getDefault()
    {
        return this.def;
    }

    public boolean hasThreads(OptionValues cli)
    {
        return cli.isPresent(ThreadsLaunchHelper.THREADS);
    }

    public boolean isUncapped(OptionValues cli)
    {
        return cli.isPresent(ThreadsLaunchHelper.UNCAP_THREADS);
    }

    protected int computeMax(OptionValues cli)
    {
        return (isUncapped(cli) ? Integer.MAX_VALUE : this.max);
    }

    public Integer getThreads(OptionValues cli)
    {
        Integer t = cli.getInteger(ThreadsLaunchHelper.THREADS);
        if (t == null)
        {
            return this.def;
        }
        return Tools.ensureBetween(this.min, t.intValue(), computeMax(cli));
    }

    public int getThreads(OptionValues cli, int def)
    {
        return Tools.ensureBetween(this.min, cli.getInteger(ThreadsLaunchHelper.THREADS, def), computeMax(cli));
    }

    @Override
    public OptionGroup asGroup(String name)
    {
        return this.group.getCopy(name);
    }
}
