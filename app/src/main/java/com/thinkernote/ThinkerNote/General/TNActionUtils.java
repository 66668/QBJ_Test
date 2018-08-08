package com.thinkernote.ThinkerNote.General;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Utils.MLog;

import java.util.Vector;

//TODO delete
public class TNActionUtils {
	private static final String TAG = "TNActionUtils";
	
	public static void stopSynchronizing(){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if(action.type == TNActionType.Synchronize 
						|| action.type == TNActionType.SynchronizeCat
						|| action.type == TNActionType.SynchronizeEdit
						|| action.type == TNActionType.GetAllData
						|| action.type == TNActionType.GetNoteList
						|| action.type == TNActionType.GetParentFolders
						|| action.type == TNActionType.GetTagList){
					if( action.isAsync()){
						action.cancel();
					}
				}
			}
		}		
	}
	//TODO 删
	public static boolean isSynchronizing(){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if(action.type == TNActionType.Synchronize 
						|| action.type == TNActionType.SynchronizeCat
						|| action.type == TNActionType.SynchronizeEdit
						|| action.type == TNActionType.GetAllData
						|| action.type == TNActionType.GetNoteList
						|| action.type == TNActionType.GetParentFolders
						|| action.type == TNActionType.GetTagList
						|| action.type == TNActionType.GetNoteByNoteId
						|| action.type == TNActionType.SyncNoteAtt
						|| action.type == TNActionType.GetAllDataByNoteId){
					MLog.d(TAG, "isSynchronizing:" + action.type);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isSynchronizing(TNAction curAction){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( !action.equals(curAction) && (
						action.type == TNActionType.Synchronize
						|| action.type == TNActionType.SynchronizeCat
						|| action.type == TNActionType.SynchronizeEdit
						|| action.type == TNActionType.GetAllData
						|| action.type == TNActionType.GetNoteList
						|| action.type == TNActionType.GetParentFolders
						|| action.type == TNActionType.GetTagList
						|| action.type == TNActionType.GetNoteByNoteId
						|| action.type == TNActionType.SyncNoteAtt
						|| action.type == TNActionType.GetAllDataByNoteId)){
					MLog.i(TAG, "isSynchronizing" + action.type + action + curAction);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isNoteSynchronizing(){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if(action.type == TNActionType.GetNoteByNoteId
						|| action.type == TNActionType.SyncNoteAtt
						|| action.type == TNActionType.GetAllDataByNoteId){
					MLog.d(TAG, "isNoteSynchronizing:" + action.type);
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isSynchroniz(TNAction aAction){
		return aAction.type == TNActionType.Synchronize
				|| aAction.type == TNActionType.SynchronizeCat
				|| aAction.type == TNActionType.SynchronizeEdit
				|| aAction.type == TNActionType.GetAllData
				|| aAction.type == TNActionType.GetNoteList
				|| aAction.type == TNActionType.GetParentFolders
				|| aAction.type == TNActionType.GetTagList
				|| aAction.type == TNActionType.GetAllDataByNoteId;
	}

	public static boolean isDownloadingAtt(long id){
		if (id == -1) 
			return false;
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if(action.type == TNActionType.TNHttpDownloadAtt){
					long attId = (Long) action.inputs.get(1);
					if( attId == id){
						MLog.e(TAG, action.type + " downloading:" + id);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isSyncingNote(long id){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( action.type == TNActionType.GetAllDataByNoteId){
					long noteId = (Long)action.inputs.get(0);
					if( noteId == id)
						return true;
				}
			}
		}
		return false;
	}
	
	public static void stopSyncing(){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( action.type == TNActionType.Synchronize 
						|| action.type == TNActionType.SynchronizeCat
						|| action.type == TNActionType.SynchronizeEdit
						|| action.type == TNActionType.GetAllData
						|| action.type == TNActionType.GetNoteList
						|| action.type == TNActionType.GetParentFolders
						|| action.type == TNActionType.GetTagList
						|| action.type == TNActionType.GetAllDataByNoteId){
					if( action.isAsync() ){
						action.cancel();
					}
				}
			}
		}		
	}

	public static void stopNoteSyncing(){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( action.type == TNActionType.GetNoteByNoteId
						|| action.type == TNActionType.SyncNoteAtt
						|| action.type == TNActionType.GetAllDataByNoteId){
					if( action.isAsync() ){
						action.cancel();
					}
				}
			}
		}
	}
	
	public static boolean isSyncing(){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( action.type == TNActionType.Synchronize 
						|| action.type == TNActionType.SynchronizeCat
						|| action.type == TNActionType.SynchronizeEdit
						|| action.type == TNActionType.GetAllData
						|| action.type == TNActionType.GetNoteList
						|| action.type == TNActionType.GetParentFolders
						|| action.type == TNActionType.GetTagList
						|| action.type == TNActionType.GetAllDataByNoteId){
					MLog.d(TAG, "isSyncing:" + action.type);
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isSyncing(TNAction curAction){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( !action.equals(curAction) && (
						action.type == TNActionType.Synchronize 
						|| action.type == TNActionType.SynchronizeCat
						|| action.type == TNActionType.SynchronizeEdit
						|| action.type == TNActionType.GetAllData
						|| action.type == TNActionType.GetNoteList
						|| action.type == TNActionType.GetParentFolders
						|| action.type == TNActionType.GetTagList
						|| action.type == TNActionType.GetAllDataByNoteId)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static void clearActions(){
		Vector<TNAction> actions = TNAction.runningList();
			synchronized(actions){
				for(TNAction action : actions){
					if( action.isAsync() ){
						action.cancel();
					}
				}
			}
			actions.clear();
	}
	
	public static boolean isRunning(Object aType){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( action.type == aType){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isRunning(Object aType, boolean needCheckStatus){
		Vector<TNAction> actions = TNAction.runningList();
		synchronized(actions){
			for(TNAction action : actions){
				if( action.type == aType){
					if(needCheckStatus)
						return isWorking(action);
					else
						return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isWorking(TNAction aAction){
		return aAction.result == TNActionResult.Working
				|| aAction.result == TNActionResult.NotStart
				|| aAction.result == TNActionResult.Waitting;
	}
	
	public static boolean isGetPushInfo(TNAction aAction){
		TNAction act = aAction;
		while(act != null && act.type != null){
//			if(act.type == TNActionType.GetPushInfo){
//				return true;
//			}
			act = act.parentAction;
		}
		return false;
	}
}
