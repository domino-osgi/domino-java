package domino.java.internal;

/**
 * A lightweight wrapper around a logging API, if available.
 * <p>
 * We only support methods with Java varargs and expect, that logging calls are
 * not time-critical.
 * <p>
 * All log messages support SLF4j-style placeholders for arguments.
 * (See also: http://www.slf4j.org/manual.html)
 * You can use a <code>{}</code> (opening curly brace immediately followed by a closing one)
 * in the message which gets replaced by the next argument.
 * Too much placeholders will be left as is.
 * Arguments not belonging to a placeholder are ignored.
 * If the last argument is a {@link Throwable}, it will be not treated as
 * argument for placeholders but
 * as cause of the log message (and will be logged specially).
 */
public interface Logger {

	boolean isErrorEnabled();

	boolean isWarnEnabled();

	boolean isInfoEnabled();

	boolean isDebugEnabled();

	boolean isTraceEnabled();

	void error(String msg, Object... args);

	void warn(String msg, Object... args);

	void info(String msg, Object... args);

	void debug(String msg, Object... args);

	void trace(String msg, Object... args);

}
