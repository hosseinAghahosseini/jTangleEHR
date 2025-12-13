/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 * @author hosseinAghahosseini
 */
public class DecryptedNode extends Node {
    
    String DecryptedEHR;
    
    public DecryptedNode(Node other, String decryptedEHR) {
    if (other == null) {
        throw new IllegalArgumentException("Cannot create a Node from a null reference.");
    }
    
    this.DecryptedEHR = decryptedEHR;

    this.NodeId = other.NodeId;

    this.OwnWeight = other.OwnWeight;
    this.CumulativeWeight = other.CumulativeWeight;
    this.Score = other.Score;

    this.FirstAcceptedNode = other.FirstAcceptedNode;
    this.FirstAcceptedNodeId = other.FirstAcceptedNodeId;
    this.SecondAcceptedNode = other.SecondAcceptedNode;
    this.SecondAcceptedNodeId = other.SecondAcceptedNodeId;

    this.PatientPreviousNode = other.PatientPreviousNode;
    this.PatientPreviousNodeId = other.PatientPreviousNodeId;
    this.PatientHashedPublicKey = other.PatientHashedPublicKey;

    this.DoctorPreviousNode = other.DoctorPreviousNode;
    this.DoctorPreviousNodeId = other.DoctorPreviousNodeId;
    this.DoctorHashedPublicKey = other.DoctorHashedPublicKey;

    this.HospitalPreviousNode = other.HospitalPreviousNode;
    this.HospitalPreviousNodeId = other.HospitalPreviousNodeId;
    this.HospitalHashedPublicKey = other.HospitalHashedPublicKey;

    this.EHR = other.EHR;
    this.IsEhrEncrypted = other.IsEhrEncrypted;
    this.EHRAesKeyEncryptedByAssymeticKey = other.EHRAesKeyEncryptedByAssymeticKey;
    this.TransactionCreatorPublicKey = other.TransactionCreatorPublicKey;

    this.Nonce = other.Nonce;
    this.Hash = other.Hash;
    this.DigitalSignature = other.DigitalSignature;

    this.state = other.state;
}
    
}
