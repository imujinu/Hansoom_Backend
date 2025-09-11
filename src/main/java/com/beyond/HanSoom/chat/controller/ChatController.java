package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.req.ChatActivateReqDto;
import com.beyond.HanSoom.chat.dto.req.ChatAnnouncementReqDto;
import com.beyond.HanSoom.chat.dto.req.ChatCreateReqDto;
import com.beyond.HanSoom.chat.dto.res.ChatAnnouncementResDto;
import com.beyond.HanSoom.chat.dto.res.*;
import com.beyond.HanSoom.chat.service.ChatPublishService;
import com.beyond.HanSoom.chat.service.ChatService;
import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatPublishService producer;
    private final SimpMessageSendingOperations messageTemplate;

    // 예약완료 후 1:1 채팅방 생성
    @PostMapping("/room/create")
    public ResponseEntity<?> createChatRoom(@RequestBody ChatCreateReqDto dto){
        System.out.println("로직이 동작합니다!");
        Long chatRoomId = chatService.createChatRoom(dto);
        return new ResponseEntity<>(new CommonSuccessDto(chatRoomId, HttpStatus.CREATED.value(), "채팅방 생성 완료"), HttpStatus.CREATED);
    }
    //단체 채팅방 가입
    @PostMapping("/room/join/{reservationId}")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long reservationId){
        Long chatRoomId = chatService.joinGroupChatRoom(reservationId);
        return new ResponseEntity<>(new CommonSuccessDto(chatRoomId, HttpStatus.OK.value(), "단체 채팅방 입장 완료"), HttpStatus.OK);
    }

    //단체 채팅방 생성
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom (){
        chatService.createGroupRoom();
        return ResponseEntity.ok().build();
    }
    //메시지 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId){
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    //나의 채팅 방 조회
    @GetMapping("/room/private/list")
    public ResponseEntity<?> getMyChatPrivateRooms(){
        List<ChatMyChatroomResDto> dtos = chatService.getMyChatRoom();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "채팅방 조회완료"), HttpStatus.OK);
    }

    //전체 채팅 방 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getMyChatGroupRooms(){
        List<ChatMyChatroomResDto> dtos = chatService.getMyGroupChatRoom();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "채팅방 조회완료"), HttpStatus.OK);
    }
    //채팅 내역 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageResDto> chatMessageDtos = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

    //호스트 호텔 내역 조회
    @GetMapping("/host/hotel")
    public ResponseEntity<?> getHostHotel() {
        List<ChatHotelResDto> dtos = chatService.getHostHotelList();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "내 호텔 조회 완료"), HttpStatus.OK);
    }

    //호스트 채팅 내역 조회
    @GetMapping("/host/all")
    public ResponseEntity<?> getHostPrivateChatRoom() {
        List<ChatHostChatRoomResDto> dto = chatService.getHostChatRoom();
        return new ResponseEntity<>(new CommonSuccessDto(dto, HttpStatus.OK.value(), "1:1 채팅 조회 완료"), HttpStatus.OK);
    }

    //호스트 단체 채팅 생성
    @PostMapping("/host/create")
    public ResponseEntity<?> createHostGroupChat() {
         chatService.createHostGroupChat();
        return new ResponseEntity<>(new CommonSuccessDto("", HttpStatus.OK.value(), "단체 채팅 생성 완료"), HttpStatus.OK);
    }

    //단체 채팅 멤버 조회
    @GetMapping("/room/{roomId}/group-user-list")
    public ResponseEntity<?> getGroupChatUserList(@PathVariable Long roomId){
        List<ChatGroupUserListResDto> dtos = chatService.getGroupChatUserList(roomId);
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "단체 채팅 멤버 조회 완료"), HttpStatus.OK);
    }

    // 공지사항 추가
    @PostMapping("/host/announcement")
    public ResponseEntity<?> addChatAnnouncement(@RequestBody ChatAnnouncementReqDto dto){
        ChatAnnouncementResDto dtos = chatService.addChatAnnouncement(dto);

        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "공지사항 추가 완료"), HttpStatus.OK);
    }

    //공지사항 조회
    @GetMapping("/host/announcements")
    public ResponseEntity<?> getChatAnnouncements(){
        List<ChatAnnouncementResDto> dtos = chatService.getChatAnnouncements();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "공지사항 조회 완료"), HttpStatus.OK);
    }

    //공지사항 삭제
    @DeleteMapping("/host/announcement/delete")
    public ResponseEntity<?> deleteChatAnnouncements(@RequestParam Long id){
        chatService.deleteChatAnnouncements(id);
        return new ResponseEntity<>(new CommonSuccessDto("", HttpStatus.OK.value(), "공지사항 삭제 성공"), HttpStatus.OK);
    }

    // 공지사항 활성화 정보 업데이트
    @PatchMapping("/host/announcements/activate")
    public ResponseEntity<?> activateChatAnnouncements(@RequestBody ChatActivateReqDto dto){
        chatService.activateChatAnnouncements(dto);
        return new ResponseEntity<>(new CommonSuccessDto("", HttpStatus.OK.value(), "공지사항 활성화 정보 업데이트"), HttpStatus.OK);
    }

    //특정 채팅방의 공지사항 목록 가져오기
    @GetMapping("/announcements/{roomId}")
    public ResponseEntity<?> getChatRoomAnnouncements(@PathVariable Long roomId){
        List<ChatAnnouncementResDto> dtos = chatService.getChatRoomAnnouncements(roomId);
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "공지사항 조회 완료"), HttpStatus.OK);
    }
    @DeleteMapping("/announcements/delete")
    public ResponseEntity<?> deleteChatAnnouncements(@RequestBody List<Long> announcements){
        chatService.deleteChatAnnouncements(announcements);
        return new ResponseEntity<>(new CommonSuccessDto("dtos", HttpStatus.OK.value(), "공지사항 삭제 완료"), HttpStatus.OK);
    }

    //사용자가 가입된 모든 채팅방 번호 조회
    @GetMapping("/user/rooms")
    public ResponseEntity<?> getAllChatRooms(){
        List<Long> chatRoomIds = chatService.getAllChatRooms();
        return new ResponseEntity<>(new CommonSuccessDto(chatRoomIds, HttpStatus.OK.value(), "채팅방 아이디 조회 완료"), HttpStatus.OK);
    }

    //채팅 금지 시간 조회
    @GetMapping("/user/remaining/{roomId}")
    public ResponseEntity<?> getRemaining(@PathVariable Long roomId){
        Long remaining = chatService.getRemaining(roomId);
        return new ResponseEntity<>(new CommonSuccessDto(remaining, HttpStatus.OK.value(), "채팅 금지 시간 조회 완료"), HttpStatus.OK);
    }

    //키 조회
    @GetMapping("/room/{roomId}/keys")
    public ResponseEntity<?> getKeys(@PathVariable Long roomId){
        ChatKeyResDto dto = chatService.getKeys(roomId);
        return new ResponseEntity<>(new CommonSuccessDto(dto, HttpStatus.OK.value(), "채팅 키 조회 완료"), HttpStatus.OK);
    }

    //채팅방 탈퇴
    @PatchMapping("/exit/{roomId}")
    public ResponseEntity<?> exitRoom(@PathVariable Long roomId){
         chatService.exitRoom(roomId);
        return new ResponseEntity<>(new CommonSuccessDto("dto", HttpStatus.OK.value(), "채팅방 탈퇴 완료"), HttpStatus.OK);
    }

}
