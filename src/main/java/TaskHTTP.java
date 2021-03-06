import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by nano on 15.10.15.
 */

public class TaskHTTP implements Runnable {
    
    private static final String DEFAULT_PATH = "DOCUMENT_ROOT";
    private Socket socket;
    private InputStream is;
    private OutputStream os;

    public TaskHTTP(Socket socket) {
        this.socket = socket;
        try {
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {

            String header = takeHeader();
            String method = takeMethod(header);

            if( method != null && !method.equals("GET") && !method.equals("HEAD")) {
                AnotherMethod();
            } else {
                AllowedMethod(method, header);
            }

        }  catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    private void AllowedMethod(String method, String header) throws IOException {

        String url = findFilePath(header);
        int status;

        // check for index file
        if (url.charAt(url.length()-1) == '/') {
            url=url+"index.html";
            if (fileExists(url)) {
                status = 200;
            } else {
                status = 403;
            }
        } else  {
            int from;
            from = url.length()-1;
            int i  = from;
            boolean pointIsFound = false;
            while (i>=0 && !pointIsFound) {
                if (url.charAt(i) == '.'){
                    pointIsFound = true;
                }
                i--;
            }
            if (!pointIsFound) {
                url = url+"index.html";

                if (fileExists(url)) {
                    status = 200;
                } else {
                    status = 403;
                }
            } else {
                status = getStatus(DEFAULT_PATH + url);
            }

            if (status == 403 && (url.indexOf("/index.html") !=-1) ) {
                url = url.substring(0,url.indexOf("/index.html"));
                if (fileExists(url)) {
                    status = 200;
                }
            }
        }

        long contentLength = getContentLength(url);
        String contentType = getContentType(url);

        String responseHeader = creatingHeader(status, contentLength, contentType);
        PrintStream answer = new PrintStream(os, true, "utf-8");
        answer.print(responseHeader);

        if( method != null && method.equals("GET") && status == 200) {
            InputStream inputStream = TaskHTTP.class.getResourceAsStream(DEFAULT_PATH+url);
            byte[] bytes = new byte[1024];
            while( inputStream.read(bytes) != -1) {
                try {
                    os.write(bytes);
                } catch (Exception e) {
                    os.flush();
                }

            }
        }
    }

    private void AnotherMethod() throws UnsupportedEncodingException {
        StringBuffer response = new StringBuffer();
        response.append("HTTP/1.1 405 Method Not Allowed\n");
        response.append("Server: TPserver\n");
        response.append("Date: " + new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss").format(new Date())+"\n");
        response.append("Connection: close\r\n\r\n");
        PrintStream answer = new PrintStream(os, true, "utf-8");
        answer.print(response.toString());
    }

    private String takeMethod(String firstLine) { // возвращает метод запроса
        int from = 0;
        int to = firstLine.indexOf(" ");
        return from <= to ? firstLine.substring(from,to) : null;
    }

    private String takeHeader() throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String ln = null;
        try {
            ln = reader.readLine();
        } catch (Exception e) {}
        while (ln != null && ln.length() != 0) {
            builder.append(ln+ "\n");
            ln = reader.readLine();
        }
        return builder.toString();
    }

    private String findFilePath(String header) {
        String url = getUrl(header);
        url = myUrlDecoder(url);
        url = removeQuery(url);
        url = removeDepricatedSymbols(url);
        return url;
    }

    private String removeQuery(String url) { // удаляет из пути get параметры запроса
        if (url.indexOf('?') != -1) {
            url = url.substring(0,url.indexOf('?'));
        }
        return url;
    }

    private String myUrlDecoder(String url) {
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    private String removeDepricatedSymbols(String url) { // удаление недопустимых символов
        boolean checked = false;
        int from;
        while (!checked) {
            if((from = url.indexOf("../") )!=-1) {
                url = url.substring(0,from)+url.substring(from+3,url.length());
            } else checked = true;
        }
        return url;
    }

    private String getUrl(String header) {
        int from = header.indexOf(" ")+1;
        int to = header.indexOf("HTTP/1.", from)-1;
        String data;

        try {
            data = header.substring(from, to);
        } catch (Exception e) {
            data = "/";
        }
        return data;
    }

    private boolean fileExists(String url) {
        InputStream inputStream = TaskHTTP.class.getResourceAsStream(DEFAULT_PATH+url);
        return inputStream != null ? true : false;
    }

    private int getStatus(String url) {
        InputStream inputStream = TaskHTTP.class.getResourceAsStream(url);
        return inputStream != null ? 200 : 404;
    }

    private String creatingHeader(int status, long contentLength, String contentType) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("HTTP/1.0 " + status + getStatusDescription(status)+"\r\n");
        buffer.append("Server: TPserver\r\n");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        buffer.append("Date: " + dateFormat.format(new Date())+"\r\n");
        buffer.append("Content-Length: " + contentLength +"\r\n");
        buffer.append("Content-Type: " + contentType +"\r\n");
        buffer.append("Connection: close\r\n\r\n");
        return buffer.toString();
    }

    private String getStatusDescription(int status) {
        if (status ==  200 )
            return " OK";
        if (status == 404)
            return " Not Found";
        if (status == 403)
            return " Forbidden";
        else return " ";
    }

    private String getContentType(String url) {
        int len = url.length()-1;
        int i = len;
        String ct = null;
        boolean ctFinded = false;
        while (i >= 0 && !ctFinded) {
            if (url.charAt(i) == '.') {
                ctFinded = true;
                ct = url.substring(i+1,len+1);
            }
            i--;
        }
        if(!ctFinded) {
            ct = "bin file";
        }
        return getFullContentType(ct);
    }

    private String getFullContentType(String end) {
        switch (end) {
            case "css": return "text/css";
            case "gif": return "image/gif";
            case "html": return "text/html";
            case "jpeg": return "image/jpeg";
            case "jpg": return "image/jpeg";
            case "js": return "text/javascript";
            case "png": return "image/png";
            case "swf": return "application/x-shockwave-flash";
            default: return end;
        }
    }
    private long getContentLength(String url) {
        String filePath = "./src/main/resources/"+DEFAULT_PATH+url;
        File requstedFile = new File(filePath);
        return requstedFile.length();
    }
}
