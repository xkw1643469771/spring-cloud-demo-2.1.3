/** JavaScriptDemo 测试方法 */
({
    test1 : function(map){
        this.demo.debug(this);
        var obj = this.demo.jsCall();
        print(obj)
        this.demo.jsCall(function(res){
            print(this, res);
        });
        map.name="map";
        this.toArr(map.arr).forEach(function(e, i){
            print(e, i);
            return false;
        });
        return map;
    },
    test2 : function(obj){
        this.toArr(obj).forEach(function(e){
            print(e);
            return false;
        });
        this.toObj(obj).forEach(function(e){
            print(e)
            return false;
        });
        return obj;
    }
})
