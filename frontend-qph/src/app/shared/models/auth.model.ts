export interface AuthRequest {
  username: string;
  password: string;
}

export type TwoFactorType =
  | 'EMAIL_AUTH'
  | 'TOTP_AUTH';

export interface AuthResponse {
  token: string | null;
  twoFactorRequired: boolean;
  challengeId: string | null;
  expiresInSeconds: number | null;
}

export interface TwoFactorVerifyRequest {
  challengeId: string;
}