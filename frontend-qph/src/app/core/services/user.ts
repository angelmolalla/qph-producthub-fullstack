import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  CreateUserRequest,
  UpdateUserRequest,
  User
} from '../../shared/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl =
    'http://localhost:8080/api/users';

  constructor(
    private http: HttpClient
  ) { }

  getAll():
    Observable<User[]> {

    return this.http
      .get<User[]>(
        this.apiUrl
      );
  }

  getById(
    id: number
  ): Observable<User> {

    return this.http
      .get<User>(
        `${this.apiUrl}/${id}`
      );
  }

  create(
    request:
      CreateUserRequest
  ): Observable<User> {

    return this.http
      .post<User>(
        this.apiUrl,
        request
      );
  }

  update(
    id: number,
    request:
      UpdateUserRequest
  ): Observable<User> {

    return this.http
      .put<User>(
        `${this.apiUrl}/${id}`,
        request
      );
  }

  delete(
    id: number
  ): Observable<void> {

    return this.http
      .delete<void>(
        `${this.apiUrl}/${id}`
      );
  }
}