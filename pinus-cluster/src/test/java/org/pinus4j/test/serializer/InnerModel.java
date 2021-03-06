package org.pinus4j.test.serializer;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InnerModel implements Serializable, Comparable<InnerModel> {

    private static final long serialVersionUID = -6212501743856350593L;

    private boolean           bool;
    private boolean           nbool;
    private boolean[]         bools;
    private boolean[]         nbools;
    private Boolean           obool;
    private Boolean           nobool;
    private Boolean[]         obools;
    private Boolean[]         nobools;

    private char              ch;
    private char              nch;
    private char[]            chs;
    private char[]            nchs;
    private Character         och;
    private Character         noch;
    private Character[]       ochs;
    private Character[]       nochs;

    private byte              b;
    private byte              nb;
    private byte[]            bs;
    private byte[]            nbs;
    private Byte              ob;
    private Byte[]            nob;
    private Byte[]            obs;
    private Byte[]            nobs;

    private short             sh;
    private short             nsh;
    private short[]           shs;
    private short[]           nshs;
    private Short             osh;
    private Short             nosh;
    private Short[]           oshs;
    private Short[]           noshs;

    private int               i;
    private int               ni;
    private int[]             is;
    private int[]             nis;
    private Integer           oi;
    private Integer           noi;
    private Integer[]         ois;
    private Integer[]         nois;

    private long              l;
    private long              nl;
    private long[]            ls;
    private long[]            nls;
    private Long              ol;
    private Long              nol;
    private Long[]            ols;
    private Long[]            nols;

    private float             f;
    private float             nf;
    private float[]           fs;
    private float[]           nfs;
    private Float             of;
    private Float             nof;
    private Float[]           ofs;
    private Float[]           nofs;

    private double            d;
    private double            nd;
    private double[]          ds;
    private double[]          nds;
    private Double            od;
    private Double            nod;
    private Double[]          ods;
    private Double[]          nods;

    private String            s;
    private String            ns;
    private String[]          ss;
    private String[]          nss;

    private String            s1;
    private String            s2;
    private String            s3;

    private Date              date;
    private Date[]            dates;
    private java.sql.Date     sqlDate;
    private java.sql.Date[]   sqlDates;
    private Calendar          cal;
    private Calendar[]        cals;
    private Timestamp         time;
    private Timestamp[]       times;

    private EnumTest          et;
    private EnumTest[]        ets;

    @Override
    public int compareTo(InnerModel o) {
        return 0;
    }

}
