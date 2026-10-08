package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.data.repository.ClipboardRepository
import com.bastyoliva.wearable.data.repository.FileManagerRepository
import com.bastyoliva.wearable.menu.MenuMode
import com.bastyoliva.wearable.menu.presentation.MenuAction
import com.bastyoliva.wearable.menu.presentation.MenuActionType
import javax.inject.Inject

class GetMenuActionsUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository,
    private val clipboardRepository: ClipboardRepository,
    private val checkAllPinnedUseCase: CheckAllPinnedUseCase,
    private val checkAllUnpinnedUseCase: CheckAllUnpinnedUseCase,
) {
    suspend operator fun invoke(paths: List<String>, mode: MenuMode): List<MenuAction> {

        val isAllPinned = checkAllPinnedUseCase(paths)
        val isAllUnpinned = checkAllUnpinnedUseCase(paths)

        val menuList = when (mode) {
            MenuMode.INSIDE -> {
                buildList {

                    if (paths.size == 1){
                        if (fileManagerRepository.canCreateDirHere(paths.first())) {
                            add(MenuAction(MenuActionType.NEW_DIR, paths))
                        }
                    }

                    if (clipboardRepository.operation != null) {
                        add(MenuAction(MenuActionType.PASTE, paths))
                    }

                }
            }

            MenuMode.OUTSIDE -> {
                buildList {
                    if (paths.size == 1){
                        add(MenuAction(MenuActionType.OPEN, paths))

                        if (fileManagerRepository.canBeRenamed(paths)) {
                            add(MenuAction(MenuActionType.RENAME, paths))
                        }
                    }

                    if (clipboardRepository.operation == null) {
                        add(MenuAction(MenuActionType.CUT, paths))
                        add(MenuAction(MenuActionType.COPY, paths))
                    } else {
                        add(MenuAction(MenuActionType.CANCEL_PASTE, paths))
                    }

                    if (isAllPinned){
                        add(MenuAction(MenuActionType.UNPIN, paths))
                    }

                    if (isAllUnpinned){
                        add(MenuAction(MenuActionType.PIN, paths))
                    }

                    if (fileManagerRepository.canBeDeleted(paths)) {
                        add(MenuAction(MenuActionType.DELETE, paths))
                    }
                }
            }

            MenuMode.PINNED -> {
                buildList {
                    if (paths.size == 1){
                        add(MenuAction(MenuActionType.OPEN, paths))
                    }

                    if (isAllPinned){
                        add(MenuAction(MenuActionType.UNPIN, paths))
                    }else{
                        add(MenuAction(MenuActionType.PIN, paths))
                    }

                }
            }
        }.toMutableList()



        return menuList

    }
}

