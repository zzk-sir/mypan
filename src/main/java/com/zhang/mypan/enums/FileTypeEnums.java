package com.zhang.mypan.enums;


public enum FileTypeEnums {
    // 1:视频， 2：音频 3：图片 4：pdf 5：word 6：excel 7：txt 8：code 9：zip 10：其他文件
    VIDEO(FileCategoryEnums.VIDEO, 1, new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"}, "视频文件"),
    MUSIC(FileCategoryEnums.MISIC, 2, new String[]{".mp3", ".wav", ".wma", ".mp2", ".flac", ".midi", ".ra", ".ape", ".aac", ".cda"}, "音频"),
    IMAGE(FileCategoryEnums.IMAGE, 3, new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".psd", ".raw", ".tiff", ".ico", ".dds", ".heif", ".indd", ".eps", ".ai", ".cdr", ".svg", ".psd", ".raw", ".tiff", ".ico", "dds"}, "图片"),
    PDF(FileCategoryEnums.DOC, 4, new String[]{".pdf"}, "pdf"),
    WORD(FileCategoryEnums.DOC, 5, new String[]{".doc", ".docx"}, "word"),
    EXCEL(FileCategoryEnums.DOC, 6, new String[]{".xls", ".xlsx"}, "excel"),
    TXT(FileCategoryEnums.DOC, 7, new String[]{".txt"}, "txt文本"),
    PROGRAME(FileCategoryEnums.OTHERS, 8, new String[]{
            ".java", ".c", ".cpp", ".h", ".hpp", ".m", ".mm", ".cs", ".vb", ".php",
            ".py", ".rb", ".sh", ".bash", ".pl", ".lua", ".go", ".swift", ".kt", ".scala", ".js", ".ts", ".html",
            ".css", ".scss", ".sass", ".less", ".json", ".xml", ".yaml", ".yml", ".ini", ".cfg", ".conf",
            ".properties", ".bat", ".cmd", ".ps1", ".vbs", ".ahk", ".coffee", ".dart", ".groovy", ".rust",
            ".sql", ".as", ".jsp", ".jspx", ".ejs", ".jspf", ".jspxf", ".aspx", ".ascx", ".master", ".vbhtml",
            ".razor", ".cshtml", ".dhtml", ".phtml", ".php3", ".php4", ".php5", ".php7", ".phps", ".pm",
            ".tcl", ".mxml", ".swc", ".cfm", ".cfc", ".cfml", ".tpl", ".twig", ".liquid", ".lasso",
            ".lassoapp", ".r", ".rmd", ".ipynb", ".kl", ".kts", ".qml", ".pro", ".qrc", ".ui",
            ".svelte", ".v", ".sv", ".svh", ".vhd", ".vhdl", ".ucf", ".qsf", ".do", ".xdc",
            ".xdcinc", ".sdc", ".pdc", ".sdcf", ".sdf", ".sdo", ".sdpf", ".vho", ".xci", ".vm",
            ".vh", ".vbx", ".veo", ".vpp", ".vcom", ".vlog", ".vp", ".vpf", ".vsd", ".vst",
            ".vsvg", ".vt", ".syn", ".vdb", ".ngc", ".ngd", ".ncd", ".twx", ".twr", ".cf",
            ".nc", ".xco", ".edn", ".xise", ".xpr", ".bld", ".syr", ".ngm", ".xrpt", ".ico",
            ".com", ".pif", ".ws", ".scf", ".lnk", ".app", ".isp", ".jar", ".cgi", ".dll",
            ".class", ".pyc", ".pyd", ".pyo", ".pyw", ".rll", ".so", ".drv", ".efi", ".sys",
            ".scr", ".vxd", ".386", ".mui", ".xex", ".xap", ".appx", ".appxbundle", ".appxupload",
            ".msi", ".msp", ".mst", ".paf", ".command", ".dmg", ".pkg", ".bin", ".iso", ".toast",
            ".vcd", ".vmdk", ".vdi", ".hdd", ".vmem", ".vmsd", ".nvram", ".vmsn", ".vmss", ".vmtm",
            ".vswp"}, "CODE"),
    ZIP(FileCategoryEnums.OTHERS, 9, new String[]{".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".war", ".ear", ".aar",}, "压缩包"),
    OTHERS(FileCategoryEnums.OTHERS, 10, new String[]{}, "其他");


    private final FileCategoryEnums category;
    private final Integer type;
    private final String[] suffixs;
    private final String desc;

    FileTypeEnums(FileCategoryEnums category, Integer type, String[] suffixs, String desc) {
        this.category = category;
        this.type = type;
        this.suffixs = suffixs;
        this.desc = desc;
    }

    public FileCategoryEnums getCategory() {
        return category;
    }

    public Integer getType() {
        return type;
    }

    public String[] getSuffixs() {
        return suffixs;
    }

    public String getDesc() {
        return desc;
    }

    public static FileTypeEnums getFileTypeBySuffix(String suffix) {
        for (FileTypeEnums fileTypeEnums : values()) {
            for (String s : fileTypeEnums.getSuffixs()) {
                if (s.equalsIgnoreCase(suffix)) {
                    return fileTypeEnums;
                }
            }
        }
        return FileTypeEnums.OTHERS;
    }

}
