package com.example1.service;

import com.example1.model.User;
import com.example1.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UserServiceImpl.class)
public class UserServiceImplTest1 {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUserByIdWithDifferentInputs() {
        User user1 = new User(1L, "John Doe", "john.doe@example.com");
        User user2 = new User(2L, "Jane Smith", "jane.smith@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        User foundUser1 = userService.getUserById(1L);
        User foundUser2 = userService.getUserById(2L);
        assertNotNull(foundUser1);
        assertEquals("John Doe", foundUser1.getName());
        assertNotNull(foundUser2);
        assertEquals("Jane Smith", foundUser2.getName());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserByIdThrowsException() {
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));
        userService.getUserById(1L);
    }

    @Test
    public void testNoInteractionWithRepository() {
        userService.getUserById(999L);
        verify(userRepository, never()).findById(999L);
    }

    @Test
    public void testUsingSpy() {
        UserServiceImpl spyUserService = spy(new UserServiceImpl(userRepository));
        User user = new User(1L, "John Doe", "john.doe@example.com");
        doReturn(Optional.of(user)).when(spyUserService).getUserById(1L);
        User foundUser = spyUserService.getUserById(1L);
        assertNotNull(foundUser);
        assertEquals("John Doe", foundUser.getName());
    }

    @Test
    public void testArgumentCaptor() {
        User user = new User(1L, "John Doe", "john.doe@example.com");
        userService.createUser(user);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User capturedUser = captor.getValue();
        assertEquals("John Doe", capturedUser.getName());
    }

    @Test
    public void testMethodCallOrder() {
        User user = new User(1L, "John Doe", "john.doe@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.getUserById(1L);
        userService.createUser(user);
        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).findById(1L);
        inOrder.verify(userRepository).save(user);
    }

    @Test
    public void testDeleteUserById() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUserById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteUserByIdThrowsException() {
        doThrow(new RuntimeException("User not found")).when(userRepository).deleteById(1L);
        userService.deleteUserById(1L);
    }

    @Test
    public void testConsecutiveCalls() {
        User user = new User(1L, "John Doe", "john.doe@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user)).thenThrow(new RuntimeException("User not found"));
        User firstCall = userService.getUserById(1L);
        assertNotNull(firstCall);
        try {
            userService.getUserById(1L);
        } catch (RuntimeException e) {
            assertEquals("User not found", e.getMessage());
        }
        verify(userRepository, times(2)).findById(1L);
    }

    @Test
    public void testMockPrivateMethodThrowsException() throws Exception {
        UserServiceImpl spyUserService = spy(new UserServiceImpl(userRepository));
        // Mock the private method to throw an exception
        PowerMockito.doThrow(new IllegalArgumentException("Mocked Exception")).when(spyUserService, "privateMethod", "input");
        try {
            spyUserService.publicMethod("input");
            //fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Mocked Exception", e.getMessage());
        }
    }
}
