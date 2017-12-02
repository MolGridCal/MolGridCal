/*
 * MolGridCal
 * Copyright MolGridCal Team
 * http://molgridcal.codeplex.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package GridFDock;

public enum Status {
	Remote_File_Noexist,	
	Local_Bigger_Remote,	
	Download_From_Break_Success,	
	Download_From_Break_Failed,		
	Download_New_Success,			
	Download_New_Failed,			
	
	
	Create_Directory_Fail,		
	Create_Directory_Success,	
	Upload_New_File_Success,	
	Upload_New_File_Failed,		
	File_Exits,					
	Remote_Bigger_Local,		
	Upload_From_Break_Success,	
	Upload_From_Break_Failed,	
	Delete_Remote_Faild;	
}