package ru.apolyakov.example.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class ConvertingUtils {
    /**
     * Converts a byte array with 4 elements to an int. Used to put ints into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). <br/>
     * Taken from http://www.daniweb.com/code/snippet216874.html
     *
     * @param data the input byte array
     * @return the resulting int
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(int)
     */
    public static int toInt(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        return (int) ( // NOTE: type cast not necessary for int
                (0xff & data[0]) << 24 |
                        (0xff & data[1]) << 16 |
                        (0xff & data[2]) << 8 |
                        (0xff & data[3]) << 0
        );
    }

    /**
     * Converts an int to a byte array with 4 elements. Used to put ints into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). <br/>
     * Taken from http://www.daniweb.com/code/snippet216874.html
     *
     * @param data the int to convert
     * @return the resulting byte[] array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toInt(byte[])
     */
    public static byte[] toBytes(int data) {

        return new byte[]{
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff),
        };
    }

    /**
     * Converts a long to a byte[] array.<br/>
     * Taken from http://www.daniweb.com/software-development/java/code/216874
     *
     * @param data the long to convert
     * @return the resulting byte[] array
     * @see #toLong(byte[])
     */
    public static byte[] toBytes(long data) {
        return new byte[]{
                (byte) ((data >> 56) & 0xff),
                (byte) ((data >> 48) & 0xff),
                (byte) ((data >> 40) & 0xff),
                (byte) ((data >> 32) & 0xff),
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff),
        };
    }
    /**
     * Converts an int to a byte array with 4 elements. Used to put ints into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). <br/>
     * Taken from http://www.daniweb.com/code/snippet216874.html
     *
     * @param data the int to convert
     * @return the resulting byte[] array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toInt(byte[])
     */
    public static byte[] toBytes(short data) {
        return new byte[]{
                (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff),
        };
    }

    public static short toShort(byte[] data) {
        if (data == null || data.length != 2) return 0x0;
        return (short) ( // NOTE: type cast not necessary for int
                (0xff & data[0]) << 8 |
                        (0xff & data[1]) << 0
        );
    }

    public static short[] toShortArray(byte[] in, int offset, int length) {
        short[] result = new short[(length >> 1)];
        byte[] tmp = new byte[2];
        for (int i = 0; i < length >> 1; i++) {
            System.arraycopy(in, offset + (i * 2), tmp, 0, 2);
            result[i] = toShort(tmp);
        }
        return result;
    }


    /**
     * Converts a byte[] array with size 8 to a long. <br/>
     * Taken from http://www.daniweb.com/software-development/java/code/216874
     *
     * @param data the byte[] array to convert
     * @return the resulting long.
     * @see #toBytes(long)
     */
    public static long toLong(byte[] data) {
        if (data == null || data.length != 8) return 0x0;
        // ----------
        return (long) (
                // (Below) convert to longs before shift because digits
                //         are lost with ints beyond the 32-bit limit
                (long) (0xff & data[0]) << 56 |
                        (long) (0xff & data[1]) << 48 |
                        (long) (0xff & data[2]) << 40 |
                        (long) (0xff & data[3]) << 32 |
                        (long) (0xff & data[4]) << 24 |
                        (long) (0xff & data[5]) << 16 |
                        (long) (0xff & data[6]) << 8 |
                        (long) (0xff & data[7]) << 0
        );
    }

    /**
     * Convenience method to transform an int[] array to a byte array for serialization.
     *
     * @param data the int[] to convert
     * @return the resulting byte[] 4 times in size (4 bytes per int)
     */
    public static byte[] toByteArray(int[] data) {
        byte[] tmp, result = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            tmp = toBytes(data[i]);
            System.arraycopy(tmp, 0, result, i * 4, 4);
        }
        return result;
    }

    /**
     * Convenience method to transform an int[] array to a byte array for serialization.
     *
     * @param data the int[] to convert
     * @return the resulting byte[] 4 times in size (4 bytes per int)
     */
    public static byte[] toByteArray(short[] data) {
        byte[] tmp, result = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            tmp = toBytes(data[i]);
            System.arraycopy(tmp, 0, result, i * 2, 2);
        }
        return result;
    }

    /**
     * Convenience method to create an int[] array from a byte[] array.
     *
     * @param data the byte[] array to decode
     * @return the decoded int[]
     */
    public static int[] toIntArray(byte[] data) {
        int[] result = new int[data.length / 4];
        byte[] tmp = new byte[4];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(data, i * 4, tmp, 0, 4);
            result[i] = toInt(tmp);
        }
        return result;
    }

    public static int[] toIntArray(byte[] in, int offset, int length) {
        int[] result = new int[(length >> 2)];
        byte[] tmp = new byte[4];
        for (int i = 0; i < length >> 2; i++) {
            System.arraycopy(in, offset + (i * 4), tmp, 0, 4);
            result[i] = toInt(tmp);
        }
        return result;
    }

    /**
     * Converts a float to a byte array with 4 elements. Used to put floats into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toFloat(byte[])} to decode.
     *
     * @param data the float to convert
     * @return the resulting byte array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toFloat(byte[])
     */
    public static byte[] toBytes(float data) {
        return toBytes(Float.floatToRawIntBits(data));
    }

    /**
     * Converts a byte array with 4 elements to a float. Used to put floats into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toBytes(float)} to encode.
     *
     * @param data the input byte array
     * @return the resulting float
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(float)
     */
    public static float toFloat(byte[] data) {
        return Float.intBitsToFloat(toInt(data));
    }

    /**
     * Convenience method for creating a byte array from a float array.
     *
     * @param data the input float array
     * @return a byte array for serialization.
     */
    public static byte[] toByteArray(float[] data) {
        byte[] tmp, result = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            tmp = toBytes(data[i]);
            System.arraycopy(tmp, 0, result, i * 4, 4);
        }
        return result;
    }

    /**
     * Convenience method for creating a float array from a byte array.
     *
     * @param data
     * @return
     */
    public static float[] toFloatArray(byte[] data) {
        float[] result = new float[data.length / 4];
        byte[] tmp = new byte[4];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(data, i * 4, tmp, 0, 4);
            result[i] = toFloat(tmp);
        }
        return result;
    }

    /**
     * Convenience method for creating a float array from a byte array.
     *
     * @param in
     * @param offset
     * @param length
     * @return
     */
    public static float[] toFloatArray(byte[] in, int offset, int length) {
        float[] result = new float[length / 4];
        byte[] tmp = new byte[4];
        for (int i = offset; i < length / 4; i++) {
            System.arraycopy(in, (i - offset) * 4 + offset, tmp, 0, 4);
            result[i] = toFloat(tmp);
        }
        return result;
    }

    /**
     * Converts a double to a byte array with 4 elements. Used to put doubles into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toDouble(byte[])} to decode. Note that there is a loss
     * in precision as the double is converted to a float in the course of conversion.
     *
     * @param data the double to convert
     * @return the resulting byte array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toDouble(byte[])
     */
    public static byte[] toBytes(double data) {
        return toBytes(Double.doubleToLongBits(data));
    }

    /**
     * Converts a byte array with 4 elements to a double. Used to put doubles into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toBytes(double)} to encode. Note that there is a loss
     * in precision as the double is converted to a float in the course of conversion.
     *
     * @param data the input byte array
     * @return the resulting float
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(double)
     */
    public static double toDouble(byte[] data) {
        return Double.longBitsToDouble(toLong(data));
    }

    /**
     * Convenience method for creating a byte array from a double array.
     *
     * @param data the input float array
     * @return a byte array for serialization.
     */
    public static byte[] toByteArray(double[] data) {
        byte[] tmp, result = new byte[data.length * 8];
        for (int i = 0; i < data.length; i++) {
            tmp = toBytes(data[i]);
            System.arraycopy(tmp, 0, result, i * 8, 8);
        }
        return result;
    }

    /**
     * Convenience method for creating a double array from a byte array.
     *
     * @param data
     * @return
     */
    public static double[] toDoubleArray(byte[] data) {
        double[] result = new double[data.length / 8];
        byte[] tmp = new byte[8];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(data, i * 8, tmp, 0, 8);
            result[i] = toDouble(tmp);
        }
        return result;
    }

    /**
     * Convenience method for creating a double array from a byte array.
     *
     * @param data
     * @return
     */
    public static double[] castToDoubleArray(byte[] data) {
        double[] result = new double[data.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    /**
     * Convenience method for creating a double array from an int array.
     *
     * @param data
     * @return
     */
    public static double[] castToDoubleArray(int[] data) {
        double[] result = new double[data.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    public static double[] castToDoubleArray(short[] data) {
        double[] result = new double[data.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    /**
     * Convenience method for creating a double array from a byte array.
     *
     * @param data
     * @param length
     * @param offset
     * @return
     */
    public static double[] toDoubleArray(byte[] data, int offset, int length) {
        double[] result = new double[length / 8];
        byte[] tmp = new byte[8];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(data, i * 8 + offset, tmp, 0, 8);
            result[i] = toDouble(tmp);
        }
        return result;
    }

    /**
     * Convenience method for creating a String from an array.
     *
     * @param array
     * @return
     */
    public static String arrayToString(int[] array) {
        return Arrays.toString(array).replace('[', ' ').replace(']', ' ').replace(',', ' ');
    }

    /**
     * Parses and returns a double array from a Sting with an arbitrary number of doubles.
     *
     * @param data
     * @return
     */
    public static double[] doubleArrayFromString(String data) {
        double[] result = null;
        LinkedList<Double> tmp = new LinkedList<Double>();
        data = data.replace('[', ' ');
        data = data.replace(']', ' ');
        data = data.replace(',', ' ');
        StringTokenizer st = new StringTokenizer(data);
        while (st.hasMoreTokens())
            tmp.add(Double.parseDouble(st.nextToken()));
        result = new double[tmp.size()];
        int i = 0;
        for (Iterator<Double> iterator = tmp.iterator(); iterator.hasNext(); ) {
            Double next = iterator.next();
            result[i] = next;
            i++;
        }
        return result;
    }

    public static double[] toDoubleArray(float[] d) {
        double[] result = new double[d.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (double) d[i];
        }
        return result;
    }

    /**
     * Create a double[] from an int[]<br/>
     * by patch contributed by Franz Graf, franz.graf@gmail.com
     *
     * @param ints the int array
     * @return a new array of doubles
     */
    public static double[] toDoubleArray(int[] ints) {
        double[] result = new double[ints.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (double) ints[i];
        }
        return result;
    }

    /**
     * Creates a double[] array from a String. It is assumed that the double array is encoded like using {@link #toString(double[])}
     *
     * @param data
     * @return
     */
    public static double[] toDoubleArray(String data) {
        LinkedList<Double> dl = new LinkedList<Double>();
        StringTokenizer st = new StringTokenizer(data);
        while (st.hasMoreTokens()) {
            dl.add(Double.parseDouble(st.nextToken()));
        }
        double[] result = new double[dl.size()];
        int count = 0;
        for (Iterator<Double> iterator = dl.iterator(); iterator.hasNext(); ) {
            double next = iterator.next();
            result[count] = next;
            count++;
        }
        return result;
    }


    /**
     * A simple string creation method. Can be parsed with {@link #toDoubleArray(String)}.
     *
     * @param data
     * @return
     */
    public static String toString(double[] data) {
        StringBuilder sb = new StringBuilder(data.length << 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String toString(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length << 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Create a hex string from an array of bytes.
     * @param data
     * @return
     */
    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length << 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02X ", data[i]));
        }
        return sb.toString().trim();
    }
}
