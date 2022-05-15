package com.example.myapp.bluetooth;

import java.util.ArrayList;

/**
 * 上位机下位机通信协议封装(Java)
 * By: ZeroHack
 * 2020/10/11
 **/

class Protocol {
    static final int CES_CMDIF_PKT_START_1 = (byte)0x0A; // Head1
    static final int CES_CMDIF_PKT_START_2 = (byte)0xFA; // Head2

    static final int CES_CMDIF_TYPE_DATA = (byte)0x02; // Packet type

    static final int CES_CMDIF_PKT_STOP_1 = (byte)0x00; // End1
    static final int CES_CMDIF_PKT_STOP_2 = (byte)0x0B; // End2
};

public class TransferController {
    public final String TAG = "Protocol-DEBUG" ;
    private int mState;
    private int mByteCnt;
    private int mIntCnt;
    private int mIntBuff;
    private IListener mListener;
    public final int DATA_LENGTH = 4;
    ArrayList<Integer> mList;

    public TransferController() {
        clear();
    }

    public void setListener(IListener listener) {
        this.mListener = listener;
    }

    public void clear() {
        mIntCnt = -1;
        mState = 0;
        mByteCnt = 0;
        mIntBuff = 0;
        mList = new ArrayList<>();
    }

    public void push_back(int chr) {
        switch (mState) {
            case 0: {
                if (chr == Protocol.CES_CMDIF_PKT_START_1) {
                    ++mState;
                    break;
                }
                clear();
                break;
            }
            case 1: {
                if (chr == Protocol.CES_CMDIF_PKT_START_2) {
                    ++mState;
                    mIntBuff = 0;
                    break;
                } else if (chr == Protocol.CES_CMDIF_PKT_START_1) {
                    mIntBuff = 0;
                    break;
                }
                clear();
                break;
            }
            case 2: {
                ++mByteCnt;
                mIntBuff >>>= 8;
                mIntBuff |= (chr & 0xFF) << 24;
                if (mByteCnt == DATA_LENGTH) {
                    mByteCnt = 0;
                    if (mIntCnt == -1) {
                        mIntCnt = mIntBuff;
                        mIntBuff = 0;
                        break;
                    }
                    mList.add(mIntBuff);
                    mIntBuff = 0;
                    if (mList.size() == mIntCnt) {
                        ++mState;
                    }
                }
                break;
            }
            case 3: {
                if (chr == Protocol.CES_CMDIF_PKT_STOP_1) {
                    ++mState;
                    break;
                }
                clear();
                break;
            }
            case 4: {
                if (chr == Protocol.CES_CMDIF_PKT_STOP_2) {
                    if (mListener != null) {
                        Integer[] dataInteger = new Integer[mIntCnt];
                        mList.toArray(dataInteger);
                        int[] data = new int[mIntCnt];
                        for (int i = 0; i < mIntCnt; ++i) {
                            data[i] = dataInteger[i];
                        }
                        mListener.onReceive(data);
                    }
                } else if (chr == Protocol.CES_CMDIF_PKT_STOP_1){
                    break ;
                }
                clear();
                break;
            }
        }
    }
}
