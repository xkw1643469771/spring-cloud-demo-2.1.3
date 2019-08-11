/** JavaScriptDemo */
(function(map){
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
})
