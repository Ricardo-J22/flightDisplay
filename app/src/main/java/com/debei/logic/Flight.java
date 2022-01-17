package com.debei.logic;

public class Flight {
    public static final int STAGE_EP = 0;//初始状态
    public static final int STAGE_SB = 1;//已准备状态
    public static final int STAGE_FA = 5;//申请第一件上架
    public static final int STAGE_FR = 6;//返回第一件上架
    public static final int STAGE_FU = 2;//正在第一件上架状态
    public static final int STAGE_LU = 3;//正在最后一件上架状态
    public static final int STAGE_LA = 7;//申请最后一件上架
    public static final int STAGE_LR = 8;//返回最后一件上架
    public static final int STAGE_FEP = 4;//清除后但未有下一班航班到来
    public static final int STAGE_CA = 9;//申请清除当前航班
    public static final int STAGE_BSB = 10;//返回准备状态
    public static final int STAGE_F = -1;//故障状态

    public static final Integer STAGE_RS = -2;

    private String flight_number;
    private int status;
    private boolean errorFlag = false;

    /**
     * 构造函数
     * @param flight_number
     * @param status
     */
    public Flight(String flight_number, int status) {
        this.flight_number = flight_number;
        this.status = status;
    }

    public boolean getErrorFlag() {
        return errorFlag;
    }

    public void setErrorFlag(boolean errorFlag) {
        this.errorFlag = errorFlag;
    }

    public String getFlight_number() {
        return flight_number;
    }

    public int getStatus() {
        return status;
    }

    public void setFlight_number(String flight_number) {
        this.flight_number = flight_number;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flight_number='" + flight_number + '\'' +
                ", status=" + status +
                ", errorFlag=" + errorFlag +
                '}';
    }
}
