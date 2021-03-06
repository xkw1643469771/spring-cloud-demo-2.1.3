/** JavaScriptDemo 公共方法 */
(function(){
    var Demo = com.xumou.demo.test.script.js.JavaScriptDemo;
    var Collection = java.util.Collection;
    return {
        demo : Demo,
        isArr : function(obj){
            if(!obj){
                return false;
            }
            if(Array.isArray(obj)){
                return true;
            }else if(Collection.class.isAssignableFrom(obj.class)){
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
                        if(Demo.call(callback, arr[i], i) === false)
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
                        if (callback.call(Demo.common, obj[name], name) === false)
                            return false;
                    return true;
                }
            }
        },
        toString : function(){
            return "common";
        }
    };
})
