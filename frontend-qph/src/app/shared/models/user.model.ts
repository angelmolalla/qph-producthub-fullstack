export type UserRole =
  | 'ADMIN'
  | 'USER';

export interface User {

  id: number;

  username: string;

  email: string | null;

  role: UserRole;

  enabled: boolean;

  twoFactorEnabled: boolean;
}

export interface CreateUserRequest {

  username: string;

  email: string;

  password: string;

  role: UserRole;

  twoFactorEnabled: boolean;
}

export interface UpdateUserRequest {

  username: string;

  email: string;

  password?: string;

  role: UserRole;

  enabled: boolean;

  twoFactorEnabled: boolean;
}