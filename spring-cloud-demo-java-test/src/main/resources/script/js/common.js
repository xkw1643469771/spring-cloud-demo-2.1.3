/** JavaScriptDemo */
({
    isArr : function(obj){
        if(!obj){
            return false;
        }
        if(Array.isArray(obj)){
            return true;
        }else if(java.util.Collection.class.isAssignableFrom(obj.class)){
            return true;
        }
    },
    toArr : function(arr){
        if(!this.isArr(arr)){
            arr = [arr];
        }
        return {
            forEach : function(callback){
                for (var i = 0; i < arr.length; i++) {
                    if(callback(arr[i], i) === false){
                        return false;
                    }
                }
                return true;
            }
        }
    },
    demo : com.xumou.demo.test.script.js.JavaScriptDemo,
})
