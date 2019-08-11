/** JavaScriptDemo */
(function(map){
    var obj = com.xumou.demo.test.script.js.JavaScriptDemo.jsCall();
    for(var i in obj){
        print("====================", i, obj[i]);
    }
    map.name="map";
    this.toArr(map.arr).forEach(function(e, i){
        print(e, i);
        return false;
    });
    return map;
})
