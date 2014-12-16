package com.google.sitebricks;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.ThreadSafe;

import com.google.common.collect.Lists;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
public class StringBufferRespond implements Respond {

	private static final String TEXT_TAG_TEMPLATE = "sitebricks.template.textfield";

	private static final String TEXTAREA_TAG_TEMPLATE = "sitebricks.template.textarea";

	// TODO Improve performance by using an insertion index rather than a placeholder string
	private static final String HEADER_PLACEHOLDER = "__sb:PLACEhOlDeR:__";

	private static final AtomicReference<Map<String, String>> templates = new AtomicReference<Map<String, String>>();

	private static final String TEXT_HTML = "text/html;charset=utf-8";

	private Object page;

	private List<String> errors;

	@SuppressWarnings("unchecked")
	public StringBufferRespond(Object context) {
		this.page = context;
		if (null == templates.get()) {
			final Properties properties = new Properties();
			try {
				properties.load(StringBufferRespond.class.getResourceAsStream("templates.properties"));
			} catch (IOException e) {
				throw new NoSuchResourceException("Can't find templates.properties", e);
			}

			// Concurrent/idempotent
			templates.compareAndSet(null, (Map) properties);
		}
	}

	private final StringBuffer out = new StringBuffer();

	private final StringBuffer head = new StringBuffer();

	private final Set<String> requires = new LinkedHashSet<String>();

	private String redirect;

	public String getHead() {
		return head.toString();
	}

	public void write(String text) {
		out.append(text);
	}

	public HtmlTagBuilder withHtml() {
		return new HtmlBuilder();
	}

	public synchronized void write(char c) {
		out.append(c);
	}

	public synchronized void require(String require) {
		requires.add(require);
	}

	public synchronized void redirect(String to) {
		this.redirect = to;
	}

	public synchronized void writeToHead(String text) {
		head.append(text);
	}

	public synchronized void chew() {
		final int lastPosition = out.length() - 1;
		if (lastPosition >= 0) {
			out.deleteCharAt(lastPosition);
		}
	}

	public synchronized String getRedirect() {
		return redirect;
	}

	public synchronized Renderable include(String argument) {
		return null;
	}

	public synchronized String getContentType() {
		return TEXT_HTML;
	}

	public synchronized void clear() {
		if (null != out) {
			out.delete(0, out.length());
		}
		if (null != head) {
			head.delete(0, head.length());
		}
	}

	@Override
	public synchronized Object pageObject() {
		return page;
	}

	@Override
	public synchronized List<String> getErrors() {
		if (this.errors == null) {
			this.errors = Lists.newArrayList();
		}
		return this.errors;
	}

	@Override
	public synchronized void setErrors(List<String> errors) {
		this.errors = errors;
	}

	@Override
	public synchronized String toString() {
		// write requires to header first...
		for (String require : requires) {
			writeToHead(require);
		}

		// write header to placeholder...
		// TODO optimize by scanning upto <body> only (if no head)
		String output = out.toString();

		int index = out.indexOf(HEADER_PLACEHOLDER);
		if (index > 0) {
			output = output.replaceFirst(HEADER_PLACEHOLDER, head.toString());
		}

		return output;
	}

	// do NOT make this a static inner class!
	private class HtmlBuilder implements HtmlTagBuilder {

		public synchronized void textField(String bind, String value) {
			write(String.format(templates.get().get(TEXT_TAG_TEMPLATE), bind, value));
		}

		public synchronized void headerPlaceholder() {
			write(HEADER_PLACEHOLDER);
		}

		public synchronized void textArea(String bind, String value) {
			write(String.format(templates.get().get(TEXTAREA_TAG_TEMPLATE), bind, value));
		}
	}

}
