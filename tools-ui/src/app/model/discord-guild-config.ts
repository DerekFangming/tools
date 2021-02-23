export class DiscordGuildConfig {
  id: string;
  welcomeEnabled: boolean;
  welcomeTitle: string;
  welcomeDescription: string;
  welcomeThumbnail: string;
  welcomeFooter: string;
  welcomeChannelId: string;
  welcomeRoleId: string;
  birthdayEnabled: boolean;
  birthdayMessage: string;
  birthdayRoleId: string;
  birthdayChannelId: string;
  roleEnabled: boolean;
  roleLevelRequirement: number;
  roleNameBlacklist: string;
  roleColorBlacklist: string;
  roleLevelRankRoleId: string;
  roleBoostRankRoleId: string;
  channelEnabled: boolean;
  channelBoostCatId: string;
  channelTempCatId: string;
}