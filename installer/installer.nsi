# This installs two files, app.exe and logo.ico, creates a start menu shortcut, builds an uninstaller, and
# adds uninstall information to the registry for Add/Remove Programs
 
;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"
  !include "nsDialogs.nsh"
  !include LogicLib.nsh
!include "nsProcess.nsh"

;Custom

; Window handle of the custom page
Var hwnd
; Install type variables
Var expressInst

Var ourdriveUrl
Var ourdriveUsername
Var ourdrivePassword
Var ourdriveImportFolder
Var ourdriveIntrayFolder

;--------------------------------
;General

!define APPNAME "Ourdrive"
!define COMPANYNAME "Cygnet"
!define DESCRIPTION "Cygnet ECM Hot folder synchronization"

# These three must be integers
!define VERSIONMAJOR 1
!define VERSIONMINOR 0
!define VERSIONBUILD 0
!define VERSION "${VERSIONMAJOR}.${VERSIONMINOR}.${VERSIONBUILD}"

# These will be displayed by the "Click here for support information" link in "Add/Remove Programs"
# It is possible to use "mailto:" links in here to open the email client
!define HELPURL "http://www.cygnet-ecm.com" # "Support Information" link
!define UPDATEURL "http://www.cygnet-ecm.com" # "Product Updates" link
!define ABOUTURL "http://www.cygnet-ecm.com" # "Publisher" link

# This is the size (in kB) of all the files copied into "Program Files"
!define INSTALLSIZE 7233

!define JRE_VERSION "1.8"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=52252"
!include "JREDyna_Inetc.nsh"

  ;Name and file
  Name "${COMPANYNAME} - ${APPNAME}"
  Icon "ourdrive.ico"
  OutFile "..\target\ourdrive-setup.exe"

  Caption "${DESCRIPTION}"
  VIProductVersion "${VERSION}.0"
  VIAddVersionKey ProductName "${APPNAME}"
  VIAddVersionKey Comments ""
  VIAddVersionKey CompanyName ${COMPANYNAME}
  VIAddVersionKey LegalCopyright ${COMPANYNAME}
  VIAddVersionKey FileDescription "${DESCRIPTION}"
  VIAddVersionKey FileVersion ${VERSION}
  VIAddVersionKey ProductVersion ${VERSION}
  VIAddVersionKey InternalName "${APPNAME}"
  VIAddVersionKey LegalTrademarks "${APPNAME} is a Trademark of ${COMPANYNAME}"
  VIAddVersionKey OriginalFilename "${APPNAME}.exe"

  ;Default installation folder
  InstallDir "$LOCALAPPDATA\${COMPANYNAME}\${APPNAME}"

  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\${COMPANYNAME}\${APPNAME}" ""

  ;Request application privileges for Windows Vista
  RequestExecutionLevel user

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_TEXT "Start and configure OurDrive"
!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchOurDrive"

  !insertmacro MUI_PAGE_LICENSE "license.rtf"
  !insertmacro CUSTOM_PAGE_JREINFO
  !insertmacro MUI_PAGE_DIRECTORY

  Page custom ChooseInstallType LeaveChooseInstallType

  Page custom EnterOurdriveParams LeaveEnterOurdriveParams

  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "Install" SecDummy

  Processes::KillProcess "ourdrive"
  ${nsProcess::KillProcess} "ourdrive.exe" $R0
  ${nsProcess::Unload}

  SetOutPath "$INSTDIR"

  call DownloadAndInstallJREIfNecessary
  	# Files for the install directory - to build the installer, these should be in the same directory as the install script (this file)
  	setOutPath $INSTDIR
  	# Files added here should be removed by the uninstaller (see section "uninstall")
  	file "ourdrive.exe"
  	file "ourdrive.ico"
  	file /oname=ourdrive.jar "..\target\ourdrive-*-SNAPSHOT-jar-with-dependencies"


  	file "ourdrive.ini"
  	# Add any other files for the install directory (license files, app data, etc) here

  	# Uninstaller - See function un.onInit and section "uninstall" for configuration
  	writeUninstaller "$INSTDIR\uninstall.exe"

  	# Start Menu
  	createDirectory "$SMPROGRAMS\${COMPANYNAME}"
  	createShortCut "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk" "$INSTDIR\ourdrive.exe" "" "$INSTDIR\ourdrive.ico"
  	createShortCut "$SMPROGRAMS\${COMPANYNAME}\Uninstall Ourdrive.lnk" "$INSTDIR\uninstall.exe" "" ""

  	# Registry information for add/remove programs
  	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "DisplayName" "${COMPANYNAME} - ${APPNAME} - ${DESCRIPTION}"
  	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
  	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
  	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "InstallLocation" "$\"$INSTDIR$\""
  	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "DisplayIcon" "$\"$INSTDIR\ourdrive.ico$\""
  	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "Publisher" "${COMPANYNAME}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "HelpLink" "${HELPURL}"
  	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "URLUpdateInfo" "${UPDATEURL}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "URLInfoAbout" "${ABOUTURL}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "DisplayVersion" "${VERSIONMAJOR}.${VERSIONMINOR}.${VERSIONBUILD}"
  	WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "VersionMajor" ${VERSIONMAJOR}
  	WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "VersionMinor" ${VERSIONMINOR}
  	# There is no option for modifying or repairing the install
  	WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "NoModify" 1
  	WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "NoRepair" 1
  	# Set the INSTALLSIZE constant (!defined at the top of this script) so Add/Remove Programs can accurately report the size
  	WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}" "EstimatedSize" ${INSTALLSIZE}

  	createShortCut "$SMPROGRAMS\Startup\OurDrive.lnk" "$INSTDIR\ourdrive.exe" "" "ourdrive.ico" "" "" "" ""

  ;Store installation folder
  WriteRegStr HKCU "Software\${COMPANYNAME}\${APPNAME}" "" $INSTDIR

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\uninstall.exe"

SectionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

    Processes::KillProcess "ourdrive"
    ${nsProcess::KillProcess} "ourdrive.exe" $R0
    ${nsProcess::Unload}

  # Remove Start Menu launcher
  	delete "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk"
  	delete "$SMPROGRAMS\${COMPANYNAME}\Uninstall Ourdrive.lnk"
  	delete "$SMPROGRAMS\Startup\OurDrive.lnk"
  	# Try to remove the Start Menu folder - this will only happen if it is empty
  	rmDir "$SMPROGRAMS\${COMPANYNAME}"

  	# Remove files
  	delete $INSTDIR\ourdrive.exe
  	delete $INSTDIR\ourdrive.ico
  	delete $INSTDIR\ourdrive.jar
  	delete $INSTDIR\ourdrive.ini

  	rmDir /r $INSTDIR\log

  	# Always delete uninstaller as the last action
  	delete $INSTDIR\uninstall.exe

  	# Try to remove the install directory - this will only happen if it is empty
  	rmDir $INSTDIR

        # Remove the default folders
        rmDir "$DESKTOP\Cygnet Cloud Intray"
  	rmDir "$DESKTOP\Cygnet Cloud Folder Upload"

  	# Remove uninstaller information from the registry
  	DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}\${APPNAME}"

  	DeleteRegKey /ifempty HKCU "Software\${COMPANYNAME}\${APPNAME}"
  	DeleteRegKey /ifempty HKCU "Software\${COMPANYNAME}"
  	DeleteRegKey HKCU "Software\JavaSoft\Prefs\com.cygnet.ourdrive"

SectionEnd


; Called before anything else as installer initialises
Function .onInit
 
  ; ExtrAPP InstallOptions files
  ; $PLUGINSDIR will automatically be removed when the installer closes
  InitPluginsDir
  File /oname=$PLUGINSDIR\installtype.ini "installtype.ini"
  File /oname=$PLUGINSDIR\ourdriveparams.ini "ourdriveparams.ini"
 
FunctionEnd


Function LaunchOurDrive

${If} $expressInst == false
Exec "$\"$INSTDIR\ourdrive.exe$\" /C"
${Else}
Exec "$\"$INSTDIR\ourdrive.exe$\" /U $\"$ourdriveUsername$\" /P $\"$ourdrivePassword$\" /O $\"$ourdriveUrl$\" /i $\"$ourdriveImportFolder$\" /t $\"$ourdriveIntrayFolder$\""
${EndIf}
    
FunctionEnd

Function ChooseInstallType
  InstallOptions::initDialog /NOUNLOAD "$PLUGINSDIR\installtype.ini"
  ; In this mode InstallOptions returns the window handle so we can use it
  Pop $hwnd
 
  !insertmacro MUI_HEADER_TEXT "Choose Installation Type" "Please select the installation type, then click Next to proceed with the install."
 
  ; Now show the dialog and wait for it to finish
  InstallOptions::show
  ; Finally fetch the InstallOptions status value (we don't care what it is though)
  Pop $0
  
FunctionEnd

Function LeaveChooseInstallType
 
  ; At this point the user has either pressed Next or one of our custom buttons
  ; We find out which by reading from the INI file
  ReadINIStr $0 "$PLUGINSDIR\installtype.ini" "Settings" "State"
  StrCmp $0 0 validate  ; Next button?
  Abort ; Return to the page
  
validate:
 
   ReadINIStr $0 "$PLUGINSDIR\installtype.ini" "Field 2" "State"
   StrCmp $0 1 automaticInst
   ReadINIStr $0 "$PLUGINSDIR\installtype.ini" "Field 3" "State"
   StrCmp $0 1 customInst
 
automaticInst:
   StrCpy $expressInst true
   Goto done
 
customInst:
   StrCpy $expressInst false
done:
FunctionEnd


Function EnterOurdriveParams
  
${If} $expressInst == false
Abort
${EndIf}

  InstallOptions::initDialog /NOUNLOAD "$PLUGINSDIR\ourdriveparams.ini"
  ; In this mode InstallOptions returns the window handle so we can use it
  Pop $hwnd
 
  !insertmacro MUI_HEADER_TEXT "Enter Ourdrive credentials" "Please enter your Ourdrive credentials, then click Next to proceed with the install."
 
  ; Now show the dialog and wait for it to finish
  InstallOptions::show
  ; Finally fetch the InstallOptions status value (we don't care what it is though)
  Pop $0
  
FunctionEnd

Function LeaveEnterOurdriveParams
 
  ; At this point the user has either pressed Next or one of our custom buttons
  ; We find out which by reading from the INI file
  ReadINIStr $0 "$PLUGINSDIR\ourdriveparams.ini" "Settings" "State"
  StrCmp $0 0 validate  ; Next button?
  Abort ; Return to the page
  
validate:

  ReadINIStr $ourdriveUrl "$PLUGINSDIR\ourdriveparams.ini" "Field 2" "State"
  ReadINIStr $ourdriveUsername "$PLUGINSDIR\ourdriveparams.ini" "Field 4" "State"
  ReadINIStr $ourdrivePassword "$PLUGINSDIR\ourdriveparams.ini" "Field 6" "State"

  ; MessageBox MB_ICONEXCLAMATION|MB_OK "Ourdrive URL: " + $ourdriveUrl

done:

  Call ExpressInstallSteps
 
FunctionEnd

Function ExpressInstallSteps

  StrCpy $ourdriveIntrayFolder "$DESKTOP\Cygnet Cloud Intray"
  StrCpy $ourdriveImportFolder "$DESKTOP\Cygnet Cloud Folder Upload"

  createDirectory "$ourdriveIntrayFolder"
  createDirectory "$ourdriveImportFolder"

FunctionEnd

