package lyrth.makanism.api;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public enum PermissionLevel {
    // Bot Owner
    OWNER(true),

    // Guild owner/Administrator (perm)
    ADMINISTRATOR(Permission.ADMINISTRATOR),

    // Manage Server (perm)
    MODERATOR(Permission.MANAGE_GUILD),

    // Everyone
    GENERAL(),

    // Custom perms
    CUSTOM()
    ;

    private boolean botOwnerOnly = true;
    private boolean any = true;  // any permission or all perms
    private HashSet<Permission> permissions = new LinkedHashSet<>();

    PermissionLevel(boolean botOwnerOnly){
        this.botOwnerOnly = botOwnerOnly;
    }

    PermissionLevel(Permission... perms){
        permissions.addAll(Arrays.asList(perms));
    }

    PermissionLevel(boolean any, Permission... perms){
        permissions.addAll(Arrays.asList(perms));
        this.any = any;
    }

    public static PermissionLevel custom(boolean any, Permission... perms){
        PermissionLevel permissionLevel = PermissionLevel.CUSTOM;
        permissionLevel.permissions.addAll(Arrays.asList(perms));
        permissionLevel.any = any;
        return permissionLevel;
    }

    public boolean isBotOwnerOnly() {
        return botOwnerOnly;
    }

    public boolean isAny() {
        return any;
    }

    public HashSet<Permission> getPermissions() {
        return permissions;
    }

    public PermissionSet getPermissionSet(){
        return PermissionSet.of(permissions.toArray(new Permission[]{}));
    }

    public Mono<Boolean> allows(Member member){
        if (botOwnerOnly)
            return Mono.just(member.getId().asLong() == 368727799189733376L); //TODO: get ID from storage
        return member.getBasePermissions()
            .flatMapIterable(PermissionSet::asEnumSet)
            .filter(permissions::contains)
            .count().cast(Integer.class)
            .map(count -> any ? count > 1 : count == permissions.size());
    }

    public Mono<Boolean> allows(User user){
        if (botOwnerOnly)
            return Mono.just(user.getId().asLong() == 368727799189733376L); //TODO: get ID from storage
        return Mono.just(permissions.size() == 0);
    }
}
