package com.nxttxn.vramel.util;

import com.nxttxn.vramel.Exchange;
import com.nxttxn.vramel.Message;
import com.nxttxn.vramel.RuntimeVramelException;
import com.nxttxn.vramel.VramelExecutionException;
import org.apache.camel.WrappedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: chuck
 * Date: 6/26/13
 * Time: 12:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectHelper {
    private static final transient Logger LOG = LoggerFactory.getLogger(ObjectHelper.class);
    private static final String DEFAULT_DELIMITER = ",";
    @SuppressWarnings("unchecked")
    private static final List<?> PRIMITIVE_ARRAY_TYPES = Arrays.asList(byte[].class, short[].class, int[].class, long[].class, float[].class, double[].class, char[].class, boolean[].class);
    /**
     * Evaluate the value as a predicate which attempts to convert the value to
     * a boolean otherwise true is returned if the value is not null
     */
    public static boolean evaluateValuePredicate(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            if ("true".equalsIgnoreCase((String)value)) {
                return true;
            } else if ("false".equalsIgnoreCase((String)value)) {
                return false;
            }
        } else if (value instanceof Collection) {
            // is it an empty collection
            Collection<?> col = (Collection<?>) value;
            return col.size() > 0;
        }
        return value != null;
    }



    /**
     * A helper method for comparing objects for equality while handling nulls
     */
    public static boolean equal(Object a, Object b) {
        if (a == b) {
            return true;
        }

        if (a instanceof byte[] && b instanceof byte[]) {
            return equalByteArray((byte[])a, (byte[])b);
        }

        return a != null && b != null && a.equals(b);
    }

    public static boolean notEqual(Object leftValue, Object rightValue) {
        return !equal(leftValue, rightValue);
    }
    /**
     * A helper method for comparing byte arrays for equality while handling
     * nulls
     */
    public static boolean equalByteArray(byte[] a, byte[] b) {
        if (a == b) {
            return true;
        }

        // loop and compare each byte
        if (a != null && b != null && a.length == b.length) {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            // all bytes are equal
            return true;
        }

        return false;
    }

    /**
     * Returns true if the given object is equal to any of the expected value
     */
    public static boolean isEqualToAny(Object object, Object... values) {
        for (Object value : values) {
            if (equal(object, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A helper method for performing an ordered comparison on the objects
     * handling nulls and objects which do not handle sorting gracefully
     */
    public static int compare(Object a, Object b) {
        return compare(a, b, false);
    }

    /**
     * A helper method for performing an ordered comparison on the objects
     * handling nulls and objects which do not handle sorting gracefully
     *
     * @param a  the first object
     * @param b  the second object
     * @param ignoreCase  ignore case for string comparison
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compare(Object a, Object b, boolean ignoreCase) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }

        if (ignoreCase && a instanceof String && b instanceof String) {
            return ((String) a).compareToIgnoreCase((String) b);
        }
        if (a instanceof Comparable) {
            Comparable comparable = (Comparable)a;
            return comparable.compareTo(b);
        }
        int answer = a.getClass().getName().compareTo(b.getClass().getName());
        if (answer == 0) {
            answer = a.hashCode() - b.hashCode();
        }
        return answer;
    }

    /**
     * Returns true if the collection contains the specified value
     */
    public static boolean contains(Object collectionOrArray, Object value) {
        if (collectionOrArray instanceof Collection) {
            Collection<?> collection = (Collection<?>)collectionOrArray;
            return collection.contains(value);
        } else if (collectionOrArray instanceof String && value instanceof String) {
            String str = (String)collectionOrArray;
            String subStr = (String)value;
            return str.contains(subStr);
        } else {
            Iterator<Object> iter = createIterator(collectionOrArray);
            while (iter.hasNext()) {
                if (equal(value, iter.next())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates an iterator over the value if the value is a collection, an
     * Object[], a String with values separated by comma,
     * or a primitive type array; otherwise to simplify the caller's code,
     * we just create a singleton collection iterator over a single value
     * <p/>
     * Will default use comma for String separating String values.
     * This method does <b>not</b> allow empty values
     *
     * @param value  the value
     * @return the iterator
     */
    public static Iterator<Object> createIterator(Object value) {
        return createIterator(value, DEFAULT_DELIMITER);
    }

    /**
     * Creates an iterator over the value if the value is a collection, an
     * Object[], a String with values separated by the given delimiter,
     * or a primitive type array; otherwise to simplify the caller's
     * code, we just create a singleton collection iterator over a single value
     * <p/>
     * This method does <b>not</b> allow empty values
     *
     * @param value      the value
     * @param delimiter  delimiter for separating String values
     * @return the iterator
     */
    public static Iterator<Object> createIterator(Object value, String delimiter) {
        return createIterator(value, delimiter, false);
    }

    /**
     * Creates an iterator over the value if the value is a collection, an
     * Object[], a String with values separated by the given delimiter,
     * or a primitive type array; otherwise to simplify the caller's
     * code, we just create a singleton collection iterator over a single value
     *
     * </p> In case of primitive type arrays the returned {@code Iterator} iterates
     * over the corresponding Java primitive wrapper objects of the given elements
     * inside the {@code value} array. That's we get an autoboxing of the primitive
     * types here for free as it's also the case in Java language itself.
     *
     * @param value             the value
     * @param delimiter         delimiter for separating String values
     * @param allowEmptyValues  whether to allow empty values
     * @return the iterator
     */
    @SuppressWarnings("unchecked")
    public static Iterator<Object> createIterator(Object value, String delimiter, final boolean allowEmptyValues) {

        // if its a message than we want to iterate its body
        if (value instanceof Message) {
            value = ((Message) value).getBody();
        }

        if (value == null) {
            return Collections.emptyList().iterator();
        } else if (value instanceof Iterator) {
            return (Iterator<Object>)value;
        } else if (value instanceof Iterable) {
            return ((Iterable<Object>)value).iterator();
        } else if (value.getClass().isArray()) {
            if (isPrimitiveArrayType(value.getClass())) {
                final Object array = value;
                return new Iterator<Object>() {
                    int idx = -1;

                    public boolean hasNext() {
                        return (idx + 1) < Array.getLength(array);
                    }

                    public Object next() {
                        idx++;
                        return Array.get(array, idx);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            } else {
                List<Object> list = Arrays.asList((Object[]) value);
                return list.iterator();
            }
        } else if (value instanceof NodeList) {
            // lets iterate through DOM results after performing XPaths
            final NodeList nodeList = (NodeList) value;
            return new Iterator<Object>() {
                int idx = -1;

                public boolean hasNext() {
                    return (idx + 1) < nodeList.getLength();
                }

                public Object next() {
                    idx++;
                    return nodeList.item(idx);
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else if (value instanceof String) {
            final String s = (String) value;

            // this code is optimized to only use a Scanner if needed, eg there is a delimiter

            if (delimiter != null && s.contains(delimiter)) {
                // use a scanner if it contains the delimiter
                Scanner scanner = new Scanner((String)value);

                if (DEFAULT_DELIMITER.equals(delimiter)) {
                    // we use the default delimiter which is a comma, then cater for bean expressions with OGNL
                    // which may have balanced parentheses pairs as well.
                    // if the value contains parentheses we need to balance those, to avoid iterating
                    // in the middle of parentheses pair, so use this regular expression (a bit hard to read)
                    // the regexp will split by comma, but honor parentheses pair that may include commas
                    // as well, eg if value = "bean=foo?method=killer(a,b),bean=bar?method=great(a,b)"
                    // then the regexp will split that into two:
                    // -> bean=foo?method=killer(a,b)
                    // -> bean=bar?method=great(a,b)
                    // http://stackoverflow.com/questions/1516090/splitting-a-title-into-separate-parts
                    delimiter = ",(?!(?:[^\\(,]|[^\\)],[^\\)])+\\))";
                }

                scanner.useDelimiter(delimiter);
                return CastUtils.cast(scanner);
            } else {
                // use a plain iterator that returns the value as is as there are only a single value
                return new Iterator<Object>() {
                    int idx = -1;

                    public boolean hasNext() {
                        return idx + 1 == 0 && (allowEmptyValues || ObjectHelper.isNotEmpty(s));
                    }

                    public Object next() {
                        idx++;
                        return s;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        } else {
            return Collections.singletonList(value).iterator();
        }
    }

    /**
     * A helper method to access a system property, catching any security exceptions
     *
     * @param name         the name of the system property required
     * @param defaultValue the default value to use if the property is not
     *                     available or a security exception prevents access
     * @return the system property value or the default value if the property is
     *         not available or security does not allow its access
     */
    public static String getSystemProperty(String name, String defaultValue) {
        try {
            return System.getProperty(name, defaultValue);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Caught security exception accessing system property: " + name + ". Will use default value: " + defaultValue, e);
            }
            return defaultValue;
        }
    }

    /**
     * Returns if the given {@code clazz} type is a Java primitive array type.
     *
     * @param clazz the Java type to be checked
     * @return {@code true} if the given type is a Java primitive array type
     */
    public static boolean isPrimitiveArrayType(Class<?> clazz) {
        return PRIMITIVE_ARRAY_TYPES.contains(clazz);
    }

    /**
     * Asserts whether the string is <b>not</b> empty.
     *
     * @param value  the string to test
     * @param name   the key that resolved the value
     * @return the passed {@code value} as is
     * @throws IllegalArgumentException is thrown if assertion fails
     */
    public static String notEmpty(String value, String name) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(name + " must be specified and not empty");
        }

        return value;
    }

    /**
     * Asserts whether the string is <b>not</b> empty.
     *
     * @param value  the string to test
     * @param on     additional description to indicate where this problem occurred (appended as toString())
     * @param name   the key that resolved the value
     * @return the passed {@code value} as is
     * @throws IllegalArgumentException is thrown if assertion fails
     */
    public static String notEmpty(String value, String name, Object on) {
        if (on == null) {
            notNull(value, name);
        } else if (isEmpty(value)) {
            throw new IllegalArgumentException(name + " must be specified and not empty on: " + on);
        }

        return value;
    }

    /**
     * Tests whether the value is <tt>null</tt> or an empty string.
     *
     * @param value  the value, if its a String it will be tested for text length as well
     * @return true if empty
     */
    public static boolean isEmpty(Object value) {
        return !isNotEmpty(value);
    }

    /**
     * Tests whether the value is <b>not</b> <tt>null</tt> or an empty string.
     *
     * @param value  the value, if its a String it will be tested for text length as well
     * @return true if <b>not</b> empty
     */
    public static boolean isNotEmpty(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof String) {
            String text = (String) value;
            return text.trim().length() > 0;
        } else {
            return true;
        }
    }

    /**
     * Asserts whether the value is <b>not</b> <tt>null</tt>
     *
     * @param value  the value to test
     * @param name   the key that resolved the value
     * @return the passed {@code value} as is
     * @throws IllegalArgumentException is thrown if assertion fails
     */
    public static Object notNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must be specified");
        }

        return value;
    }

    /**
     * Asserts whether the value is <b>not</b> <tt>null</tt>
     *
     * @param value  the value to test
     * @param on     additional description to indicate where this problem occurred (appended as toString())
     * @param name   the key that resolved the value
     * @return the passed {@code value} as is
     * @throws IllegalArgumentException is thrown if assertion fails
     */
    public static Object notNull(Object value, String name, Object on) {
        if (on == null) {
            notNull(value, name);
        } else if (value == null) {
            throw new IllegalArgumentException(name + " must be specified on: " + on);
        }

        return value;
    }

    /**
     * Returns the type name of the given value
     */
    public static String className(Object value) {
        return name(value != null ? value.getClass() : null);
    }

    /**
     * Returns the type name of the given type or null if the type variable is
     * null
     */
    public static String name(Class<?> type) {
        return type != null ? type.getName() : null;
    }

    public static String after(String text, String after) {
        if (!text.contains(after)) {
            return null;
        }
        return text.substring(text.indexOf(after) + after.length());
    }

    public static String before(String text, String before) {
        if (!text.contains(before)) {
            return null;
        }
        return text.substring(0, text.indexOf(before));
    }

    public static String capitalize(String text) {
        if (text == null) {
            return null;
        }
        int length = text.length();
        if (length == 0) {
            return text;
        }
        String answer = text.substring(0, 1).toUpperCase(Locale.ENGLISH);
        if (length > 1) {
            answer += text.substring(1, length);
        }
        return answer;
    }

    /**
     * Returns the type of the given object or null if the value is null
     */
    public static Object type(Object bean) {
        return bean != null ? bean.getClass() : null;
    }


    public static String getIdentityHashCode(Object object) {
        return "0x" + Integer.toHexString(System.identityHashCode(object));
    }

    /**
     * Wraps the caused exception in a {@link RuntimeVramelException} if its not
     * already such an exception.
     *
     * @param e the caused exception
     * @return the wrapper exception
     */
    public static RuntimeVramelException wrapRuntimeCamelException(Throwable e) {
        if (e instanceof RuntimeVramelException) {
            // don't double wrap
            return (RuntimeVramelException)e;
        } else {
            return new RuntimeVramelException(e);
        }
    }

    /**
     * Wraps the caused exception in a {@link VramelExecutionException} if its not
     * already such an exception.
     *
     * @param e the caused exception
     * @return the wrapper exception
     */
    public static VramelExecutionException wrapVramelExecutionException(Exchange exchange, Throwable e) {
        if (e instanceof VramelExecutionException) {
            // don't double wrap
            return (VramelExecutionException)e;
        } else {
            return new VramelExecutionException("Exception occurred during execution", exchange, e);
        }
    }


    /**
     * Lookup the constant field on the given class with the given name
     *
     * @param clazz  the class
     * @param name   the name of the field to lookup
     * @return the value of the constant field, or <tt>null</tt> if not found
     */
    public static String lookupConstantFieldValue(Class<?> clazz, String name) {
        if (clazz == null) {
            return null;
        }

        // remove leading dots
        if (name.startsWith(",")) {
            name = name.substring(1);
        }

        for (Field field : clazz.getFields()) {
            if (field.getName().equals(name)) {
                try {
                    Object v = field.get(null);
                    return v.toString();
                } catch (IllegalAccessException e) {
                    // ignore
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Does the given class have a default public no-arg constructor.
     */
    public static boolean hasDefaultPublicNoArgConstructor(Class<?> type) {
        // getConstructors() returns only public constructors
        for (Constructor<?> ctr : type.getConstructors()) {
            if (ctr.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the canonical type name of the given value
     */
    public static String classCanonicalName(Object value) {
        if (value != null) {
            return value.getClass().getCanonicalName();
        } else {
            return null;
        }
    }

    /**
     * Returns true if the given collection of annotations matches the given type
     */
    public static boolean hasAnnotation(Annotation[] annotations, Class<?> type) {
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }


    public static String between(String text, String after, String before) {
        text = after(text, after);
        if (text == null) {
            return null;
        }
        return before(text, before);
    }

    /**
     * Tests whether the target method overrides the source method.
     * <p/>
     * Tests whether they have the same name, return type, and parameter list.
     *
     * @param source  the source method
     * @param target  the target method
     * @return <tt>true</tt> if it override, <tt>false</tt> otherwise
     */
    public static boolean isOverridingMethod(Method source, Method target) {
        if (source.getName().equals(target.getName())
                && source.getReturnType().equals(target.getReturnType())
                && source.getParameterTypes().length == target.getParameterTypes().length) {

            // test if parameter types is the same as well
            for (int i = 0; i < source.getParameterTypes().length; i++) {
                if (!(source.getParameterTypes()[i].equals(target.getParameterTypes()[i]))) {
                    return false;
                }
            }

            // the have same name, return type and parameter list, so its overriding
            return true;
        }

        return false;
    }

    /**
     * Retrieves the given exception type from the exception.
     * <p/>
     * Is used to get the caused exception that typically have been wrapped in some sort
     * of Camel wrapper exception
     * <p/>
     * The strategy is to look in the exception hierarchy to find the first given cause that matches the type.
     * Will start from the bottom (the real cause) and walk upwards.
     *
     * @param type the exception type wanted to retrieve
     * @param exception the caused exception
     * @return the exception found (or <tt>null</tt> if not found in the exception hierarchy)
     */
    public static <T> T getException(Class<T> type, Throwable exception) {
        if (exception == null) {
            return null;
        }

        // walk the hierarchy and look for it
        Iterator<Throwable> it = createExceptionIterator(exception);
        while (it.hasNext()) {
            Throwable e = it.next();
            if (type.isInstance(e)) {
                return type.cast(e);
            }
        }

        // not found
        return null;
    }

    /**
     * Converts primitive types such as int to its wrapper type like
     * {@link Integer}
     */
    public static Class<?> convertPrimitiveTypeToWrapperType(Class<?> type) {
        Class<?> rc = type;
        if (type.isPrimitive()) {
            if (type == int.class) {
                rc = Integer.class;
            } else if (type == long.class) {
                rc = Long.class;
            } else if (type == double.class) {
                rc = Double.class;
            } else if (type == float.class) {
                rc = Float.class;
            } else if (type == short.class) {
                rc = Short.class;
            } else if (type == byte.class) {
                rc = Byte.class;
            } else if (type == boolean.class) {
                rc = Boolean.class;
            }
        }
        return rc;
    }

    /**
     * A helper method to create a new instance of a type using the default
     * constructor arguments.
     */
    public static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeVramelException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeVramelException(e);
        }
    }

    /**
     * Returns true if a class is assignable from another class like the
     * {@link Class#isAssignableFrom(Class)} method but which also includes
     * coercion between primitive types to deal with Java 5 primitive type
     * wrapping
     */
    public static boolean isAssignableFrom(Class<?> a, Class<?> b) {
        a = convertPrimitiveTypeToWrapperType(a);
        b = convertPrimitiveTypeToWrapperType(b);
        return a.isAssignableFrom(b);
    }


    /**
     * A helper method to create a new instance of a type using the default
     * constructor arguments.
     */
    public static <T> T newInstance(Class<?> actualType, Class<T> expectedType) {
        try {
            Object value = actualType.newInstance();
            return cast(expectedType, value);
        } catch (InstantiationException e) {
            throw new RuntimeVramelException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeVramelException(e);
        }
    }
    /**
     * Converts the given value to the required type or throw a meaningful exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Class<T> toType, Object value) {
        if (toType == boolean.class) {
            return (T)cast(Boolean.class, value);
        } else if (toType.isPrimitive()) {
            Class<?> newType = convertPrimitiveTypeToWrapperType(toType);
            if (newType != toType) {
                return (T)cast(newType, value);
            }
        }
        try {
            return toType.cast(value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Failed to convert: "
                    + value + " to type: " + toType.getName() + " due to: " + e, e);
        }
    }

    /**
     * Creates a {@link Scanner} for scanning the given value.
     *
     * @param exchange  the current exchange
     * @param value     the value, typically the message IN body
     * @return the scanner, is newer <tt>null</tt>
     */
    public static Scanner getScanner(Exchange exchange, Object value) {
        if (value instanceof WrappedFile) {
            // generic file is just a wrapper for the real file so call again with the real file
            WrappedFile<?> gf = (WrappedFile<?>) value;
            return getScanner(exchange, gf.getFile());
        }

        String charset = exchange.getProperty(Exchange.CHARSET_NAME, String.class);

        Scanner scanner = null;
        if (value instanceof Readable) {
            scanner = new Scanner((Readable)value);
        } else if (value instanceof InputStream) {
            scanner = charset == null ? new Scanner((InputStream)value) : new Scanner((InputStream)value, charset);
        } else if (value instanceof File) {
            try {
                scanner = charset == null ? new Scanner((File)value) : new Scanner((File)value, charset);
            } catch (FileNotFoundException e) {
                throw new RuntimeVramelException(e);
            }
        } else if (value instanceof String) {
            scanner = new Scanner((String)value);
        } else if (value instanceof ReadableByteChannel) {
            scanner = charset == null ? new Scanner((ReadableByteChannel)value) : new Scanner((ReadableByteChannel)value, charset);
        }

        if (scanner == null) {
            // value is not a suitable type, try to convert value to a string
            String text = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, value);
            if (text != null) {
                scanner = new Scanner(text);
            }
        }

        if (scanner == null) {
            scanner = new Scanner("");
        }

        return scanner;
    }
    /**
     * Creates an iterator to walk the exception from the bottom up
     * (the last caused by going upwards to the root exception).
     *
     * @param exception  the exception
     * @return the iterator
     */
    public static Iterator<Throwable> createExceptionIterator(Throwable exception) {
        return new ExceptionIterator(exception);
    }

    private static final class ExceptionIterator implements Iterator<Throwable> {
        private List<Throwable> tree = new ArrayList<Throwable>();
        private Iterator<Throwable> it;

        public ExceptionIterator(Throwable exception) {
            Throwable current = exception;
            // spool to the bottom of the caused by tree
            while (current != null) {
                tree.add(current);
                current = current.getCause();
            }

            // reverse tree so we go from bottom to top
            Collections.reverse(tree);
            it = tree.iterator();
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public Throwable next() {
            return it.next();
        }

        public void remove() {
            it.remove();
        }
    }
}
