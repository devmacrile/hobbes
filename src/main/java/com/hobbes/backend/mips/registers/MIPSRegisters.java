package com.hobbes.backend.mips.registers;

import java.util.Arrays;
import java.util.List;

public class MIPSRegisters {

    // general purpose registers
    public static MIPSRegister ZERO = new MIPSGRegister("zero");
    public static MIPSRegister AT = new MIPSGRegister("at");
    public static MIPSRegister V0 = new MIPSGRegister("v0");
    public static MIPSRegister V1 = new MIPSGRegister("v1");
    public static MIPSRegister A0 = new MIPSGRegister("a0");
    public static MIPSRegister A1 = new MIPSGRegister("a1");
    public static MIPSRegister A2 = new MIPSGRegister("a2");
    public static MIPSRegister A3 = new MIPSGRegister("a3");
    public static MIPSRegister T0 = new MIPSGRegister("t0");
    public static MIPSRegister T1 = new MIPSGRegister("t1");
    public static MIPSRegister T2 = new MIPSGRegister("t2");
    public static MIPSRegister T3 = new MIPSGRegister("t3");
    public static MIPSRegister T4 = new MIPSGRegister("t4");
    public static MIPSRegister T5 = new MIPSGRegister("t5");
    public static MIPSRegister T6 = new MIPSGRegister("t6");
    public static MIPSRegister T7 = new MIPSGRegister("t7");
    public static MIPSRegister T8 = new MIPSGRegister("t8");
    public static MIPSRegister T9 = new MIPSGRegister("t9");
    public static MIPSRegister S0 = new MIPSGRegister("s0");
    public static MIPSRegister S1 = new MIPSGRegister("s1");
    public static MIPSRegister S2 = new MIPSGRegister("s2");
    public static MIPSRegister S3 = new MIPSGRegister("s3");
    public static MIPSRegister S4 = new MIPSGRegister("s4");
    public static MIPSRegister S5 = new MIPSGRegister("s5");
    public static MIPSRegister S6 = new MIPSGRegister("s6");
    public static MIPSRegister S7 = new MIPSGRegister("s7");
    public static MIPSRegister K0 = new MIPSGRegister("k0");
    public static MIPSRegister K1 = new MIPSGRegister("k1");
    public static MIPSRegister GP = new MIPSGRegister("gp");
    public static MIPSRegister SP = new MIPSGRegister("sp");
    public static MIPSRegister FP = new MIPSGRegister("fp");
    public static MIPSRegister RA = new MIPSGRegister("ra");

    // general purpose semantic register collections
    public static List<MIPSRegister> functionArgumentRegisters =
            Arrays.asList(A0, A1, A2, A3);
    public static List<MIPSRegister> generalPurposeSaveRegisters =
            Arrays.asList(S0, S1, S2, S3, S4, S5, S6, S7);
    public static List<MIPSRegister> generalPurposeTempRegisters =
            Arrays.asList(T0, T1, T2, T3, T4, T5, T6, T7, T8, T9);

    // floating point registers
    public static MIPSRegister F0 = new MIPSFRegister(0);
    public static MIPSRegister F1 = new MIPSFRegister(1);
    public static MIPSRegister F2 = new MIPSFRegister(2);
    public static MIPSRegister F3 = new MIPSFRegister(3);
    public static MIPSRegister F4 = new MIPSFRegister(4);
    public static MIPSRegister F5 = new MIPSFRegister(5);
    public static MIPSRegister F6 = new MIPSFRegister(6);
    public static MIPSRegister F7 = new MIPSFRegister(7);
    public static MIPSRegister F8 = new MIPSFRegister(8);
    public static MIPSRegister F9 = new MIPSFRegister(9);
    public static MIPSRegister F10 = new MIPSFRegister(10);
    public static MIPSRegister F11 = new MIPSFRegister(11);
    public static MIPSRegister F12 = new MIPSFRegister(12);
    public static MIPSRegister F13 = new MIPSFRegister(13);
    public static MIPSRegister F14 = new MIPSFRegister(14);
    public static MIPSRegister F15 = new MIPSFRegister(15);
    public static MIPSRegister F16 = new MIPSFRegister(16);
    public static MIPSRegister F17 = new MIPSFRegister(17);
    public static MIPSRegister F18 = new MIPSFRegister(18);
    public static MIPSRegister F19 = new MIPSFRegister(19);
    public static MIPSRegister F20 = new MIPSFRegister(20);
    public static MIPSRegister F21 = new MIPSFRegister(21);
    public static MIPSRegister F22 = new MIPSFRegister(22);
    public static MIPSRegister F23 = new MIPSFRegister(23);
    public static MIPSRegister F24 = new MIPSFRegister(24);
    public static MIPSRegister F25 = new MIPSFRegister(25);
    public static MIPSRegister F26 = new MIPSFRegister(26);
    public static MIPSRegister F27 = new MIPSFRegister(27);
    public static MIPSRegister F28 = new MIPSFRegister(28);
    public static MIPSRegister F29 = new MIPSFRegister(29);
    public static MIPSRegister F30 = new MIPSFRegister(30);
    public static MIPSRegister F31 = new MIPSFRegister(31);

    // floating point semantic register collections
    public static List<MIPSRegister> floatFunctionReturnValueRegisters =
            Arrays.asList(F0, F1, F2, F3);
    public static List<MIPSRegister> floatTempRegisters =
            Arrays.asList(F4, F5, F6, F7, F8, F9, F10, F11, F16, F17, F18, F19);
    public static List<MIPSRegister> floatFunctionArgumentRegisters =
            Arrays.asList(F12, F13, F14, F15);
    public static List<MIPSRegister> floatSaveRegisters =
            Arrays.asList(F20, F21, F22, F23, F24, F25, F26, F27, F28, F29, F30);
}
