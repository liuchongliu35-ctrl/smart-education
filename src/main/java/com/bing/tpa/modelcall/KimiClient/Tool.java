package com.bing.tpa.modelcall.KimiClient;

import lombok.Data;

@Data
class Tool {
    public String type;
    public Function function;

    public Tool(String type, Function function) {
        this.type = type;
        this.function = function;
    }
}
