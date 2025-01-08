#include "NativeFileDialogs.h"
#include <windows.h>
#include <commdlg.h>
#include <string>

std::string openFileDialog(const char* filter) {
    OPENFILENAMEA ofn;
    char szFile[MAX_PATH] = { 0 };
    char currentDir[MAX_PATH] = { 0 };

    ZeroMemory(&ofn, sizeof(ofn)); 

    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = nullptr;
    ofn.lpstrFile = szFile;
    ofn.nMaxFile = sizeof(szFile);
    ofn.lpstrFilter = filter;
    ofn.nFilterIndex = 1;
    ofn.lpstrTitle = "Select a File";
    ofn.Flags = OFN_PATHMUSTEXIST | OFN_FILEMUSTEXIST | OFN_NOCHANGEDIR | OFN_HIDEREADONLY | OFN_EXPLORER | OFN_ENABLESIZING;

    if (GetCurrentDirectoryA(MAX_PATH, currentDir)) {
        ofn.lpstrInitialDir = currentDir;
    }

    if (GetOpenFileNameA(&ofn)) {
        return std::string(szFile);
    }

    return "";
}

JNIEXPORT jstring JNICALL Java_me_radu_gui_NativeFileDialogs_selectFile(JNIEnv *env, jclass clazz, jstring filter) {
    const char* nativeFilter = env->GetStringUTFChars(filter, 0);
    std::string selectedFile = openFileDialog(nativeFilter);
    env->ReleaseStringUTFChars(filter, nativeFilter);

    if (selectedFile.empty()) {
        return nullptr;
    }

    return env->NewStringUTF(selectedFile.c_str());
}
