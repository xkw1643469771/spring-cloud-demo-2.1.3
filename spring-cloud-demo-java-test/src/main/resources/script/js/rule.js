/** JavaScriptDemo */
(function(map){
    com.xumou.demo.test.script.js.JavaScriptDemo.jsCall();
    map.name="map";
    this.toArr(map.arr).forEach(function(e, i){
        print(e, i);
        return false;
    });
    return map;
})
