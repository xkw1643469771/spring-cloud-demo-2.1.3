/** JavaScriptDemo 公共方法 */
(function(){
    var demo = com.xumou.demo.test.script.js.JavaScriptDemo;
    var Collection = java.util.Collection;
    return {
        demo : demo,
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
                        if(demo.call(callback, arr[i], i) === false)
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
                        if (callback.call(demo.common, obj[name], name) === false)
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
