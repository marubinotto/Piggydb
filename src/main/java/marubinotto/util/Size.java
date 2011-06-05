package marubinotto.util;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Size implements Serializable {
	
    private long value;
    private String decimalFormat  = "0.00";

    public Size(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public double getAsGigaBytes() {
        return ByteUnit.GIGA_BYTE.convert(this.value);
    }

    public String getAsGigaBytesString() {
        DecimalFormat df = new DecimalFormat(this.decimalFormat);
        return df.format(getAsGigaBytes()) + " "	 + ByteUnit.GIGA_BYTE;
    }

    public double getAsMegaBytes() {
        return ByteUnit.MEGA_BYTE.convert(this.value);
    }

    public String getAsMegaBytesString() {
        DecimalFormat df = new DecimalFormat(this.decimalFormat);
        return df.format(getAsMegaBytes()) + " " + ByteUnit.MEGA_BYTE;
    }
    
    public double getAsKiroBytes() {
    	return ByteUnit.KIRO_BYTE.convert(this.value);
    }
    
    public String getAsKiroBytesString() {
    	DecimalFormat df = new DecimalFormat(this.decimalFormat);
        return df.format(getAsKiroBytes()) + " " + ByteUnit.KIRO_BYTE;
    }

    public String toString() {
        if (this.value > ByteUnit.GIGA_BYTE.size) {
            return getAsGigaBytesString();
        }
        else if (this.value > ByteUnit.MEGA_BYTE.size) {
        	return getAsMegaBytesString();
        }
        else {
            return getAsKiroBytesString();
        }
    }

    public static class ByteUnit {
        public static final ByteUnit BYTE = new ByteUnit("byte", 1);
        public static final ByteUnit KIRO_BYTE = new ByteUnit("KByte", 1024);
        public static final ByteUnit MEGA_BYTE = new ByteUnit("MByte", 1048576);
        public static final ByteUnit GIGA_BYTE = new ByteUnit("GByte", 1073741824);

        private String name;
        private long size;

        private ByteUnit(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public long getSize() {
            return size;
        }

        public double convert(long byteSize) {
            return (double)byteSize / (double)this.size;
        }

        public String toString() {
            return this.name;
        }
    }
}
