package org.swordapp.server.action;

import org.swordapp.server.*;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import javax.servlet.ServletInputStream;

public class ModWrapper extends HttpServletRequestWrapper
    //implements HttpServletRequest
{
    public ModWrapper(HttpServletRequest request) {
        super(request);
    }

    
    @Override
    public String getHeader(String name) {
        String header = super.getHeader(name);
        System.out.println("ModWrapper - getHeader:" 
                + " - name:" + name
                + " - header" + header
        );
        return header;
        //return (header != null) ? header : super.getParameter(name); // Note: you can't use getParameterValues() here.
    }

    @Override
    public Enumeration getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        for (String name : names) {
            System.out.println("ModWrapper - getHeaderNames:" 
                    + " - name:" + name
            );
            
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration getHeaders(String name) {
        List<String> names = Collections.list(super.getHeaders(name));
        for (String retname : names) {
            System.out.println("ModWrapper - getHeaders:" 
                    + " - retname:" + retname
            );
            
        }
        return Collections.enumeration(names);
    }
    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        Collection<Part> parts = super.getParts();
        System.out.println("ModWrapper - getParts:" 
                    + " - parts:" + parts.size()
            );
        return parts;
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        System.out.println("ModWrapper - getInputStream:"
            );
        return super.getInputStream();
    }
    
    @Override
    public Object getAttribute(String name) {
        System.out.println("ModWrapper - getAttribute:" 
                    + " - name:" + name
            );
        return super.getAttribute(name);
    }
    
    @Override
    public String getParameter(String name) {
        System.out.println("ModWrapper - getParameter:" 
                    + " - name:" + name
            );
        return super.getParameter(name);
    }
}
