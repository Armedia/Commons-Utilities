/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 *
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities.cli.launcher;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.cli.Option;
import com.armedia.commons.utilities.cli.OptionImpl;
import com.armedia.commons.utilities.cli.OptionParseResult;
import com.armedia.commons.utilities.cli.OptionParser;
import com.armedia.commons.utilities.cli.OptionScheme;
import com.armedia.commons.utilities.cli.OptionSchemeExtensionSupport;
import com.armedia.commons.utilities.cli.OptionValues;
import com.armedia.commons.utilities.cli.exception.CommandLineSyntaxException;
import com.armedia.commons.utilities.cli.exception.HelpRequestedException;
import com.armedia.commons.utilities.cli.help.HelpRenderer;
import com.armedia.commons.utilities.function.CheckedFunction;

public abstract class AbstractEntrypoint {

	private static final Option HELP_OPTION = new OptionImpl() //
		.setShortOpt('?') //
		.setLongOpt("help") //
		.setMinArguments(0) //
		.setMaxArguments(0) //
		.setDescription("Display this help message") //
	;

	private static final URL[] URLS = {};
	private static final String[] NO_ARGS = {};

	protected Logger log = Main.BOOT_LOG;
	protected Logger console = Main.BOOT_LOG;

	protected final File userDir;
	protected final File homeDir;

	protected AbstractEntrypoint() {
		String userDir = System.getProperty("user.dir");
		if (StringUtils.isEmpty(userDir)) {
			userDir = ".";
		}
		this.userDir = Tools.canonicalize(new File(userDir));
		String homeDir = System.getProperty("user.home");
		if (StringUtils.isEmpty(homeDir)) {
			homeDir = ".";
		}
		this.homeDir = Tools.canonicalize(new File(homeDir));
	}

	/**
	 * <p>
	 * Process the OptionParseResult. If an error occurs, a {@link CommandLineProcessingException}
	 * will be raised, and the exit result will be set to the value obtained from that exception's
	 * {@link CommandLineProcessingException#getReturnValue() getReturnValue()}.
	 * </p>
	 *
	 * @throws CommandLineProcessingException
	 *             if there was an error processing the command line - such as an illegal option
	 *             combination, illegal option value, etc
	 */
	protected void processCommandLineResult(OptionValues baseValues, String command, OptionValues commandValues,
		Collection<String> positionals) throws CommandLineProcessingException {
	}

	protected Option getHelpOption() {
		return AbstractEntrypoint.HELP_OPTION;
	}

	public abstract String getName();

	protected boolean initLogging(OptionValues baseValues, String command, OptionValues commandValues,
		Collection<String> positionals) {
		// By default, do nothing...
		return false;
	}

	protected Collection<? extends LaunchClasspathHelper> getClasspathHelpers(OptionValues baseValues, String command,
		OptionValues commandValues, Collection<String> positionals) {
		return Collections.emptyList();
	}

	private OptionParseResult parseArguments(Option helpOption, final OptionScheme baseScheme, String... args)
		throws CommandLineSyntaxException, HelpRequestedException {
		OptionSchemeExtensionSupport extensionSupport = null;
		if (OptionSchemeExtensionSupport.class.isInstance(this)) {
			extensionSupport = OptionSchemeExtensionSupport.class.cast(this);
		}
		return new OptionParser().parse(helpOption, baseScheme, extensionSupport, args);
	}

	protected abstract OptionScheme getOptionScheme();

	public int execute(String... args) {
		final Option helpOption = getHelpOption();
		final OptionScheme optionScheme;
		try {
			optionScheme = getOptionScheme();
		} catch (Exception e) {
			this.log.error("Failed to initialize the option scheme", e);
			return -1;
		}

		if (args == null) {
			args = AbstractEntrypoint.NO_ARGS;
		}

		final OptionParseResult result;
		try {
			result = parseArguments(helpOption, optionScheme, args);
		} catch (HelpRequestedException e) {
			HelpRenderer.renderHelp(getName(), e, System.err);
			return 1;
		} catch (CommandLineSyntaxException e) {
			HelpRenderer.renderError("ERROR", e, System.err);
			return 1;
		} catch (Throwable t) {
			this.log.error("Failed to process the command-line arguments", t);
			return -1;
		}

		try {
			processCommandLineResult(result.getOptionValues(), result.getCommand(), result.getCommandValues(),
				result.getPositionals());
		} catch (CommandLineProcessingException e) {
			this.log.error("Failed to process the command-line values", e);
			return e.getReturnValue();
		}

		Collection<? extends LaunchClasspathHelper> classpathHelpers = getClasspathHelpers(result.getOptionValues(),
			result.getCommand(), result.getCommandValues(), result.getPositionals());
		final Set<URL> classpathPatches = new LinkedHashSet<>();
		for (LaunchClasspathHelper helper : classpathHelpers) {
			final Collection<URL> extraPatches = helper.getClasspathPatches(result.getOptionValues());
			if ((extraPatches != null) && !extraPatches.isEmpty()) {
				for (URL u : extraPatches) {
					if (u != null) {
						classpathPatches.add(u);
					}
				}
			}
		}

		final CheckedFunction<OptionParseResult, Integer, Exception> entryPoint = getEntrypoint();

		final AtomicInteger ret = new AtomicInteger(0);
		final Thread mainThread = Thread.currentThread();
		final Runnable runner = () -> {
			// We have a complete command line, and the final classpath. Let's initialize
			// the logging.
			if (initLogging(result.getOptionValues(), result.getCommand(), result.getCommandValues(),
				result.getPositionals())) {
				// Retrieve the logger post-initialization...if nothing was initialized, we stick to
				// the
				// same log
				this.log = LoggerFactory.getLogger(getClass());
				this.console = LoggerFactory.getLogger("console");
			}

			// The logging is initialized, we can make use of it now.
			showBanner(this.console);
			for (URL url : classpathPatches) {
				this.console.info("Classpath addition: [{}]", url);
			}

			try {
				Integer r = entryPoint.applyChecked(result);
				if (r == null) {
					r = NumberUtils.INTEGER_ZERO;
				}
				showFooter(this.console, r);
				ret.set(r.intValue());
			} catch (Exception e) {
				showError(this.console, e);
				ret.set(1);
			}
		};

		// Check to see if we need to patch the classpath
		final ClassLoader cl = newClassLoader(mainThread.getContextClassLoader(), classpathPatches);
		if (cl != null) {
			// Classpath needs patching, so apply it...
			final Thread newThread = new Thread(mainThread.getThreadGroup(), runner,
				mainThread.getName() + "-entrypoint");
			newThread.setDaemon(mainThread.isDaemon());

			if (cl != null) {
				newThread.setContextClassLoader(cl);
			}
			newThread.start();
			try {
				newThread.join();
			} catch (InterruptedException e) {
				this.log.warn("Interrupted while waiting for the {} thread", newThread.getName());
				return 1;
			}
		} else {
			// No classpath modifications, so don't mess around with a subthread
			runner.run();
		}

		return ret.get();
	}

	private ClassLoader newClassLoader(ClassLoader parent, Set<URL> extraPaths) {
		if ((extraPaths == null) || extraPaths.isEmpty()) { return null; }
		return new URLClassLoader(extraPaths.toArray(AbstractEntrypoint.URLS), parent);
	}

	protected void showBanner(Logger log) {
		// By default, do nothing
	}

	protected void showFooter(Logger log, int rc) {
		// By default, do nothing
	}

	protected void showError(Logger log, Throwable e) {
		log.error("Exception caught", e);
	}

	protected abstract CheckedFunction<OptionParseResult, Integer, Exception> getEntrypoint();
}
