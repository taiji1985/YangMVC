<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>大文件上传实例</title>
    <script type="text/javascript">
        const BYTES_PER_CHUNK = 1024 * 1024; // 每个文件切片大小定为1MB .
        var slices;
        var totalSlices;

        //发送请求
        function sendRequest() {

            var blob = document.getElementById('file').files[0];

            var start = 0;
            var end;
            var index = 0;

            // 计算文件切片总数
            slices = Math.ceil(blob.size / BYTES_PER_CHUNK);
            totalSlices= slices;

            while(start < blob.size) {
                end = start + BYTES_PER_CHUNK;
                if(end > blob.size) {
                    end = blob.size;
                }

                uploadFile(blob, index, start, end);

                start = end;
                index++;
            }
        }

        //上传文件
        function uploadFile(blob, index, start, end) {
            var xhr;
            var fd;
            var chunk;

            xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if(xhr.readyState == 4) {
                    if(xhr.responseText) {
                        console.log(xhr.responseText);
                    }

                    slices--;

                    // 如果所有文件切片都成功发送，发送文件合并请求。
                    if(slices == 0) {
                        mergeFile(blob);
                        alert('文件上传完毕');
                    }
                }
            };


            chunk =blob.slice(start,end);//切割文件

            //构造form数据
            fd = new FormData();
            fd.append("file", chunk);
            fd.append("name", blob.name);
            fd.append("index", index);
            fd.append("total",totalSlices);

            xhr.open("POST", "upload", true);

            //设置二进制文边界件头
            xhr.setRequestHeader("X_Requested_With", location.href.split("/")[3].replace(/[^a-z]+/g, '$'));
            xhr.send(fd);
        }

        function mergeFile(blob) {
            var xhr;
            var fd;

            xhr = new XMLHttpRequest();

            fd = new FormData();
            fd.append("name", blob.name);
            fd.append("index", totalSlices);
            fd.append("total",totalSlices);

            xhr.open("POST", "upload", true);
            xhr.setRequestHeader("X_Requested_With", location.href.split("/")[3].replace(/[^a-z]+/g, '$'));
            xhr.send(fd);
        }

    </script>
</head>
<body>

<input type="file" id="file"/>
<button  onclick="sendRequest()">上传</button>
</body>
</html>