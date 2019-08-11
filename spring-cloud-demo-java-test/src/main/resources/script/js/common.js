/** JavaScriptDemo 公共方法 */
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
                if(!(typeof(callback) === "function"))
                    return false;
                for (var i = 0; i < arr.length; i++)
                    if(callback(arr[i], i) === false)
                        return false;
                return true;
            }
        }
    },
    toObj : function(obj){
        return {
            forEach : function(callback) {
                if(!(typeof(callback) === "function"))
                    return false;
                for (var name in obj)
                    if (callback(obj[name], name) === false)
                        return false;
                return true;
            }
        }
    },
    demo : com.xumou.demo.test.script.js.JavaScriptDemo,
})
