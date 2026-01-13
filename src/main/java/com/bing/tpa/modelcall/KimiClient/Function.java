package com.bing.tpa.modelcall.KimiClient;

import lombok.Data;

//tool工具，可以开启联网功能
@Data
class Function {
    public String name;

    public Function(String name) {
        this.name = name;
    }
}