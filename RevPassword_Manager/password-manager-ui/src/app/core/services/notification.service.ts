import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BehaviorSubject } from 'rxjs';
 
export interface Notification {
  id: number;
  username: string;
  message: string;
  type: string;
  readStatus: boolean;
  createdAt: string;
}
 
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
 
  private api = 'http://localhost:8080/api/notifications';
 
  constructor(private http: HttpClient) {}
 
  getNotifications(username: string): Observable<Notification[]> {
  return this.http.get<Notification[]>(`${this.api}?username=${username}`);
}
 
 
  markAsRead(id: number) {
    return this.http.put(`${this.api}/read/${id}`, {});
  }
 
  deleteNotification(id: number) {
    return this.http.delete(`${this.api}/${id}`);
  }

 private notificationCountSource = new BehaviorSubject<number>(0);
notificationCount$ = this.notificationCountSource.asObservable();

setNotificationCount(count: number) {
  this.notificationCountSource.next(count);
}
}