package com.example.myapp.bluetooth;

import java.io.InputStream;

public interface Callback {
    void onRecieve(InputStream istream);
}