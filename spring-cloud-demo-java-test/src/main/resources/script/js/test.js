/** JavaScriptTest */
(function(){
    return {
        abc : function(){
            print(123123);
        },
        clearObj : function(obj){
            //afasdfadsf
            /*// a
            asdf
            adfs
            asfdfd
            sadfasdfa

            sdfsadfsdf */
            for(var i in obj){
                print(obj[i]);
            }
        },
        // java传递参数
        params : function(){
            for (var i = 0; i < arguments.length; i++) {
                print(arguments[i]);
            }
        },
        // 长数字
        bigNumber : function(){
            return 13213213123123131231313131312312;
        }
    }
})()
